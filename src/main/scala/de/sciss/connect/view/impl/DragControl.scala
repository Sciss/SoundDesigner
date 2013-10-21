package de.sciss.connect.view.impl

import java.awt.event.MouseEvent
import prefuse.controls.ControlAdapter
import java.awt.geom.{Line2D, Ellipse2D, Area, Rectangle2D, Point2D}
import prefuse.visual.VisualItem
import java.awt.{BasicStroke, Graphics2D, Point, Toolkit, Color, RenderingHints, Cursor}
import java.awt.image.BufferedImage
import javax.swing.SwingUtilities
import prefuse.util.display.PaintListener
import prefuse.Display
import collection.immutable.{IndexedSeq => Vec}
import de.sciss.connect.view.{VisualBox, Port, VisualPorts, impl}
import de.sciss.lucre.synth.Sys

// TODO: add TableListener to react to items disappearing (see original DragControl)
object DragControl {
  private val csrPatch = {
    val img   = new BufferedImage(17, 17, BufferedImage.TYPE_INT_ARGB)
    val g     = img.createGraphics()
    val shp1  =   new Area(new Ellipse2D.Float(0, 0, 17, 17))
    shp1.subtract(new Area(new Ellipse2D.Float(5, 5,  7,  7)))
    val shp2  =   new Area(new Ellipse2D.Float(1, 1, 15, 15))
    shp2.subtract(new Area(new Ellipse2D.Float(4, 4,  9,  9)))
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setColor(Color.white)
    g.fill(shp1)
    g.setColor(Color.black)
    g.fill(shp2)
    g.dispose()
    Toolkit.getDefaultToolkit.createCustomCursor(img, new Point(8, 8), "patch")
  }

  private val strkRubber = new BasicStroke(1.5f)
}
final class DragControl[S <: Sys[S]](d: impl.PaneImpl[S]) extends ControlAdapter {
  import DragControl._

  private val _mousePoint = new Point2D.Float()  // in virtual space
  private var hover       = DragNone: DragMaybe

  private var drag        = DragNone: DragMaybe
  private val dragPoint   = new Point2D.Float
  private var dragStarted = false
  // private var dragSrcPort = Option.empty[Port]
  // private var dragSnk     = Option.empty[(VisualItem, VisualElementT[S], Port)]

  private val dragTemp    = new Point2D.Float

  def mousePoint: Point2D = new Point2D.Float(_mousePoint.x, _mousePoint.y)

  private sealed trait DragMaybe {
    def foreach(fun: DragSome => Unit): Unit
    def isBox: Boolean
  }
  private case object DragNone extends DragMaybe with DragMaybeCable with DragMaybeHover {
    def foreach(fun: DragSome => Unit) = ()
    def isBox = false
  }
  private sealed trait DragSome extends DragMaybe {
    def originVi  : VisualItem
    def originData: VisualBox[S]
    def foreach(fun: DragSome => Unit): Unit = fun(this)
  }
  private case class DragBox  (originVi: VisualItem, originData: VisualBox[S]) extends DragSome {
    def isBox = true
  }
  private sealed trait DragMaybeHover
  private sealed trait DragHoverOrCable extends DragSome with DragMaybeHover {
    def originVi  : VisualItem
    def originData: VisualBox[S]
    def originPort: Port

    def isBox = false

    def open(point: Point2D): DragCableOpen = DragCableOpen(originVi, originData, originPort, point)
  }
  private case class DragHover(originVi: VisualItem, originData: VisualBox[S], originPort: Port)
    extends DragHoverOrCable


  private sealed trait DragMaybeCable
  private sealed trait DragSomeCable extends DragHoverOrCable {
    def targetPoint: Point2D

    def originPoint: Point2D = {
      val r   = originPort.visualRect(originData.ports)
      val b   = originVi.getBounds
      val rx  = r.getCenterX + b.getX
      val ry  = r.getCenterY + b.getY
      new Point2D.Double(rx, ry)
    }
  }
  private case class DragCableOpen(originVi: VisualItem, originData: VisualBox[S], originPort: Port,
                                   targetPoint: Point2D)
    extends DragSomeCable

  private sealed trait DragCableClosed extends DragSomeCable {
    def sourceVi  : VisualItem
    def sourceData: VisualBox[S]
    def sourcePort: Port.Out
    def sinkVi    : VisualItem
    def sinkData  : VisualBox[S]
    def sinkPort  : Port.In

    def targetVi  : VisualItem
    def targetData: VisualBox[S]
    def targetPort: Port

    def targetPoint: Point2D = {
      val r   = targetPort.visualRect(targetData.ports)
      val b   = targetVi.getBounds
      val rx  = r.getCenterX + b.getX
      val ry  = r.getCenterY + b.getY
      new Point2D.Double(rx, ry)
    }
  }
  private case class DragSourceToSink(originVi: VisualItem, originData: VisualBox[S], originPort: Port.Out,
                                      targetVi: VisualItem, targetData: VisualBox[S], targetPort: Port.In)
    extends DragCableClosed {

    def sourceVi    = originVi
    def sourceData  = originData
    def sourcePort  = originPort

    def sinkVi      = targetVi
    def sinkData    = targetData
    def sinkPort    = targetPort
  }
  private case class DragSinkToSource(originVi: VisualItem, originData: VisualBox[S], originPort: Port.In,
                                      targetVi: VisualItem, targetData: VisualBox[S], targetPort: Port.Out)
    extends DragCableClosed {

    def sourceVi    = targetVi
    def sourceData  = targetData
    def sourcePort  = targetPort

    def sinkVi      = originVi
    def sinkData    = originData
    def sinkPort    = originPort
  }

  private object Rubberband extends PaintListener {
    private val line  = new Line2D.Float

    def prePaint(disp: Display, g: Graphics2D) = ()

    def postPaint(disp: Display, g: Graphics2D): Unit = drag match {
      case cable: DragSomeCable =>
        val at    = disp.getTransform
        val pt1   = cable.originPoint
        val pt2   = cable.targetPoint
        line.setLine(pt1, pt2)
        val lineO = strkRubber.createStrokedShape(line)
        val shp   = at.createTransformedShape(lineO)

        g.setColor(Style.selectionColor)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING       , RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
        g.fill(shp)
      case _ =>
    }
  }

  private def reportMouse(e: MouseEvent): Unit = {
    val at = d.display.getInverseTransform
    at.transform(e.getPoint, _mousePoint)
    //println(s"mouse screen ${e.getPoint} - virt ${_mousePoint}")
  }

  private def findPort(seq: Vec[Rectangle2D], tx: Double, ty: Double): Int = seq.indexWhere { r =>
    r.getMinX - 1 <= tx && r.getMaxX >= tx && r.getMinY - 1 <= ty && r.getMaxY >= ty
  }

  private def detectPort(ports: VisualPorts, vi: VisualItem, e: MouseEvent): Option[Port] = {
    val b     = vi.getBounds
    val tx    = _mousePoint.getX - b.getX
    val ty    = _mousePoint.getY - b.getY
    val idxIn = findPort(ports.inlets, tx, ty)
    if (idxIn >= 0) Some(Port.In(idxIn)) else {
      val idxOut = findPort(ports.outlets, tx, ty)
      if (idxOut >= 0) Some(Port.Out(idxOut)) else None
    }
  }

  private def processMove(vi: VisualItem, e: MouseEvent): Unit = {
    reportMouse(e)

    hover = d.getNodeData(vi).fold[DragMaybe](DragNone) { data =>
      val ports = data.ports
      val port  = detectPort(ports, vi, e)
      val h     = port.fold[DragMaybe](DragBox(vi, data))(DragHover(vi, data, _))
      if (ports.active != port) {
        ports.active = port
        // println("SET " + ports.active)
        vi.setValidated(false)  // force repaint
        d.visualization.repaint()
      }
      h
    }
  }

  private def updateCursor(): Unit =
    d.display.setCursor(hover match {
      case _: DragHover | _: DragCableClosed  => csrPatch
      case _                                  => Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    })

  override def itemEntered(vi: VisualItem, e: MouseEvent): Unit = {
    processMove(vi, e)
    updateCursor()
  }

  override def itemExited(vi: VisualItem, e: MouseEvent): Unit = {
    hover.foreach {
      case DragHover(vih, datah, _) =>
        datah.ports.active = None
        vih.setValidated(false)
        d.visualization.repaint()
      case _ =>
    }
    hover = DragNone
    d.display.setCursor(Cursor.getDefaultCursor)
  }

  override def itemMoved(vi: VisualItem, e: MouseEvent): Unit = {
    val oldHover = hover
    processMove(vi, e)
    if (hover != oldHover) updateCursor()
  }

  override def itemPressed(vi: VisualItem, e: MouseEvent): Unit = {
    if (!SwingUtilities.isLeftMouseButton(e)) return

    if (e.getClickCount == 2 && hover.isBox) {
      d.editObject(vi)
    } else {
      drag        = hover
      dragStarted = false
      d.display.getAbsoluteCoordinate(e.getPoint, dragPoint)
    }
  }

  override def itemReleased(vi: VisualItem, e: MouseEvent): Unit = {
    processMove(vi, e)  // might un-highlight port
    updateCursor()

    drag match {
      case closed: DragCableClosed =>
        // closed.sinkData match {
        //   case _ =>
        // }
        // TODO: verify that connection is legal
        d.connect(closed.sourceData, closed.sourcePort, closed.sinkData, closed.sinkPort)

      case _ =>
    }
    drag = DragNone
    if (dragStarted) {
      dragStarted = false
      d.display.removePaintListener(Rubberband)
      d.visualization.repaint()
    }
  }

  override def itemDragged(vi: VisualItem, e: MouseEvent): Unit = {
    reportMouse(e)

    drag.foreach { dr =>
      val ep  = e.getPoint
      d.display.getAbsoluteCoordinate(ep, dragTemp)
      val dx  = dragTemp.getX - dragPoint.getX
      val dy  = dragTemp.getY - dragPoint.getY

      if (!dragStarted) {
        val dist = dx * dx + dy * dy
        if (dist < 4) return
      }

      dr match {
        case cable: DragHoverOrCable =>
          // detect current sink
          val target = Option(d.display.findItem(ep)).fold[DragMaybeHover](DragNone) { tgtVi =>
            // don't allow direct connections within one object (at least for now)
            if (tgtVi == vi) DragNone else d.getNodeData(tgtVi).fold[DragMaybeHover](DragNone) { tgtData =>
              detectPort(tgtData.ports, tgtVi, e).fold[DragMaybeHover](DragNone)(DragHover(tgtVi, tgtData, _))
            }
          }

          // TODO: verify that connection is legal
          val newDrag = (cable.originPort, target) match {
            case (out @ Port.Out(_), DragHover(tgtVi, tgtData, in  @ Port.In (_))) =>
              DragSourceToSink(cable.originVi, cable.originData, out, tgtVi, tgtData, in )
            case (in  @ Port.In (_), DragHover(tgtVi, tgtData, out @ Port.Out(_))) =>
              DragSinkToSource(cable.originVi, cable.originData, in , tgtVi, tgtData, out)
            case _ => cable.open(dragTemp)
          }
        
          // reflect sink changes
          if (cable != newDrag) {
            cable match {
              case closed: DragCableClosed =>
                closed.targetData.ports.active = None
                closed.targetVi.setValidated(false)
              case _ =>
            }
            newDrag match {
              case closed: DragCableClosed =>
                closed.targetData.ports.active = Some(closed.targetPort)
                closed.targetVi.setValidated(false)
              case _ =>
            }
            drag = newDrag
          }

          //          target.foreach { case sink =>
          //            val b   = sink.vi.getBounds
          //            val r   = sink.port.visualRect(sink.data.ports)
          //            val rx  = r.getCenterX + b.getX
          //            val ry  = r.getCenterY + b.getY
          //            dragTemp.setLocation(rx, ry)
          //          }

        case _ =>
          val x   = vi.getX
          val y   = vi.getY

          vi.setStartX(x)
          vi.setStartY(y)
          vi.setX     (x + dx)
          vi.setY     (y + dy)
          vi.setEndX  (x + dx)
          vi.setEndY  (y + dy)

          vi.getVisualization.repaint()
      }

      if (!dragStarted) {
        dragStarted = true
        d.display.addPaintListener(Rubberband)
      }
      // Rubberband.update(drag, dragTemp)
      d.visualization.repaint()
      dragPoint.setLocation(dragTemp)
    }
  }

  override def mouseEntered(e: MouseEvent): Unit = reportMouse(e)
  override def mouseDragged(e: MouseEvent): Unit = reportMouse(e)
  override def mouseMoved(  e: MouseEvent): Unit = reportMouse(e)
}