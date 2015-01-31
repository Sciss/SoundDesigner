package de.sciss.connect
package view
package impl

import java.awt.Rectangle
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.geom.Point2D
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.{JComponent, JTextField}

import de.sciss.lucre.expr.{Int => IntEx}
import de.sciss.lucre.stm
import de.sciss.lucre.stm.IdentifierMap
import de.sciss.lucre.synth.Sys
import de.sciss.synth.proc._
import prefuse.controls.{PanControl, ZoomControl}
import prefuse.data.Graph
import prefuse.render.DefaultRendererFactory
import prefuse.visual.VisualItem
import prefuse.{Display, Visualization}

import scala.swing.event.Key
import scala.swing.{Action, Component}

// XXX TODO: requires call on EDT; iterate over initial content of patcher; disposal
object PaneImpl {
  def apply[S <: Sys[S]](patcher: Patcher[S], config: Pane.Config[S])
                        (implicit tx: S#Tx, cursor: stm.Cursor[S]): PaneImpl[S] = {
    val cueMap  = tx.newInMemoryIDMap[PutMetaData]
    val viewMap = tx.newInMemoryIDMap[VisualBox[S]]
    val res = new Impl[S](tx.newHandle(patcher), viewMap, cueMap, config)
    patcher.changed.react { implicit tx => { upd =>
      upd.changes.foreach {
        case Patcher.NodeAdded  (elem) => res.elemAdded  (elem)
        case Patcher.NodeRemoved(elem) => res.elemRemoved(elem)
        case Patcher.NodeChanged(elem, elemUpd) =>
      }
    }}

    // patcher.changed.react(_ => upd => println(s"Observed update $upd"))

    res
  }

  private case class PutMetaData(point: Point2D, edit: Boolean)

  private final val GROUP_GRAPH   = "graph"
  private final val COL_DATA      = "data"
  // private final val COL_PORTS     = "ports"

  private final class Impl[S <: Sys[S]](patcher: stm.Source[S#Tx, Patcher[S]],
                                        viewMap: IdentifierMap[S#ID, S#Tx, VisualBox[S]],
                                        cueMap : IdentifierMap[S#ID, S#Tx, PutMetaData],
                                        config : Pane.Config[S])
                                       (implicit cursor: stm.Cursor[S]) extends PaneImpl[S] {
    val imp = ExprImplicits[S]

    val visualization = new Visualization
    private val rf = new DefaultRendererFactory(new BoxRenderer(this), new CableRenderer(this))
    visualization.setRendererFactory(rf)

    private var editingNode     = Option.empty[VisualItem]
    private var editingOldText  = ""

    val dragControl   = new DragControl(this)
    val display  = {
      val res   = new Display(visualization)
      val imap  = res.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
      val amap  = res.getActionMap
      import de.sciss.desktop.KeyStrokes._
      imap.put(menu1 + Key.Key1, "designer.putObject")
      amap.put("designer.putObject", Action("putObject") {
        put(edit = true) { implicit tx => Obj(IncompleteElement[S]()) }
      }.peer)
      imap.put(menu1 + Key.Key3, "designer.putInt")
      amap.put("designer.putInt", Action("putInt") {
        put() { implicit tx => Obj(IntElem[S](IntEx.newConst(0))) }
      }.peer)
      res
    }
    private val g   = new Graph
    g .addColumn(COL_DATA , classOf[AnyRef /* VisualBox[S] */])
    /* private val vg = */ visualization.addGraph(GROUP_GRAPH, g)
    // vg.addColumn(COL_PORTS, classOf[VisualPorts])

    display.addControlListener(dragControl)
    display.addControlListener(new PanControl())
    display.addControlListener(new ZoomControl())

    private def lastMousePoint(): Point2D = dragControl.mousePoint

    private def put(edit: Boolean = false)(elem: S#Tx => Obj[S]): Unit = {
      val mp  = lastMousePoint()
      mp.setLocation(mp.getX - 2, mp.getY - 2)
      val cue = PutMetaData(mp, edit)
      cursor.step { implicit tx =>
        val e = elem(tx)
        // println(s"Put cue $cue")
        cueMap.put(e.id, cue)
        patcher().addNode(e)
      }
    }

    def elemAdded(elem: Obj[S])(implicit tx: S#Tx): Unit = elem match {
      case IntElem.Obj(a)           => addVertex(elem, new VisualInt              [S](tx.newHandle(a), a.elem.peer.value))
      case BooleanElem.Obj(a)       => addVertex(elem, new VisualBoolean          [S](tx.newHandle(a), a.elem.peer.value))
      case UGenSource.Obj(a)        => addVertex(elem, new VisualUGenSource       [S](tx.newHandle(a), a.elem.spec      ))
      case IncompleteElement.Obj(a) => addVertex(elem, new VisualIncompleteElement[S](tx.newHandle(a), a.elem.peer.value))
      case Connection.Obj(a)        => addEdge(a)
      case _                        => // ignore
    }

    private def addEdge(cObj: Connection.Obj[S])(implicit tx: S#Tx): Unit = {
      val c = cObj.elem
      // println(s"addEdge($c")
      for {
        sourceData <- viewMap.get(c.source._1.id)
        sinkData   <- viewMap.get(c.sink  ._1.id)
      } guiFromTx {
        for {
          sourceNode <- sourceData.node
          sinkNode   <- sinkData  .node
        } {
          val e     = g.addEdge(sourceNode, sinkNode)
          // println(s"edge = $e")
          val data  = VisualEdge(c.source._2, c.sink._2)
          e.set(COL_DATA, data)
        }
      }
    }

    private def addVertex(elem: Obj[S], data: VisualBox[S])(implicit tx: S#Tx): Unit = {
      viewMap.put(elem.id, data)
      val cueOpt = cueMap.get(elem.id)
      if (cueOpt.nonEmpty) cueMap.remove(elem.id)
      // println(s"Get cue $cueOpt")
      guiFromTx {
        val n       = g.addNode()
        n.set(COL_DATA, data)
        data.init(n)

        val vi      = visualization.getVisualItem(GROUP_GRAPH, n)
        val mp      = cueOpt.map(_.point).getOrElse(lastMousePoint())
        vi.setX(mp.getX)
        vi.setY(mp.getY)
        // val ports = new VisualPorts(numIns = 0, numOuts = 1)
        data.ports.update(vi.getBounds)
        // vi.set(COL_PORTS, ports)
        if (cueOpt.exists(_.edit)) editObject(vi)
        visualization.repaint()
      }
    }

    def elemRemoved(elem: Obj[S])(implicit tx: S#Tx): Unit = {
      val dataOpt = viewMap.get(elem.id)
      cueMap.remove(elem.id)

      dataOpt.foreach { data =>
        viewMap.remove(elem.id)
        guiFromTx {
          data.node.foreach { n =>
            g.removeNode(n)
            visualization.repaint()
          }
        }
      }
    }

    val component = Component.wrap(display)

    // def getNodeData (vi: VisualItem): Option[VisualBox[S]] = Option(vi.get(COL_DATA ).asInstanceOf[VisualBox[S]])
    // def getEdgeData (vi: VisualItem): Option[VisualEdge  ] = Option(vi.get(COL_DATA ).asInstanceOf[VisualEdge  ])

    def getNodeData (vi: VisualItem): Option[VisualBox[S]] = vi.get(COL_DATA) match {
      case b: VisualBox[S]  => Some(b)
      case _                => None
    }

    def getEdgeData (vi: VisualItem): Option[VisualEdge  ] = vi.get(COL_DATA) match {
      case e: VisualEdge    => Some(e)
      case _                => None
    }

    private def updateEditingBounds(vi: VisualItem): Rectangle = {
      //      vi.validateBounds()
      vi.setValidated(false)  // this causes a subsequent call to getBounds to ask the renderer again, plus creates dirty screen region
      val b      = vi.getBounds
      val at     = display.getTransform
      val r      = at.createTransformedShape(b).getBounds
      r.x       += 3
      r.y       += 1
      r.width   -= 5
      r.height  -= 2
      getNodeData(vi).foreach(_.ports.update(b))
      r
    }

    def editObject(vi: VisualItem): Unit = getNodeData(vi).foreach {
      case data: VisualIncompleteElement[S] =>
        val r = updateEditingBounds(vi)
        editingNode     = Some(vi)
        editingOldText  = data.value
        display.editText(data.value, r)
      case _ =>
    }

    def connect(source: VisualBox[S], out: Port.Out, sink: VisualBox[S], in: Port.In): Unit =
      cursor.step { implicit tx =>
        val sourceArt = source.source()
        val sinkArt   = sink  .source()
        val sourceLet = out.idx
        val sinkLet   = in .idx
        val conn      = Obj(Connection.apply(source = (sourceArt, sourceLet), sink = (sinkArt, sinkLet)))
        val p         = patcher()
        p.addNode(conn)
      }

    private def stopEditing(): Unit = {
      val txt = display.getTextEditor.getText
      display.stopEditing()
      editingNode.foreach { vi =>
        editingNode = None
        getNodeData(vi).foreach {
          case vge: VisualIncompleteElement[S] =>
            vge.value = txt
            if (vge.value != editingOldText) {

              val mp  = new Point2D.Float(vi.getX.toFloat, vi.getY.toFloat)
              val cue = PutMetaData(mp, edit = false)

              val success = cursor.step { implicit tx =>
                val completeOpt = config.factory(tx)(vge.value)
                completeOpt.foreach { complete =>
                  val incomplete  = vge.source()
                  val p           = patcher()
                  p.removeNode(incomplete)
                  cueMap.put(complete.id, cue)
                  p.addNode(complete)
                }
                completeOpt.isDefined
              }

              if (!success) {
                vge.state   = ElementState.Error
                updateEditingBounds(vi)
                visualization.repaint()
              }
            }

          case _ =>
        }
      }
    }

    display.getTextEditor match {
      case tf: JTextField =>
        tf.setFont(Style.font)
        tf.setForeground(Style.selectionColor)
        tf.setBackground(Style.boxColor)
        tf.getDocument.addDocumentListener(new DocumentListener {
          def refreshBox(): Unit =
            editingNode.foreach { vi =>
              getNodeData(vi).foreach {
                case data: VisualIncompleteElement[S] =>
                  data.value = tf.getText
                  //                vi.set(COL_ELEM, data)
                  //                vis.repaint()
                  val r = updateEditingBounds(vi)
                  //                println("BOUNDS " + r)
                  tf.setSize(r.getSize)
                  visualization.repaint()

                case _ =>
              }
          }

          def insertUpdate (e: DocumentEvent): Unit = refreshBox()
          def removeUpdate (e: DocumentEvent): Unit = refreshBox()
          def changedUpdate(e: DocumentEvent): Unit = refreshBox()
        })
        tf.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent): Unit = stopEditing()
      })
    }
  }
}
sealed trait PaneImpl[S <: Sys[S]] extends Pane[S] {
  def visualization: Visualization
  def display      : Display
  def getNodeData (vi: VisualItem): Option[VisualBox [S]]
  def getEdgeData (vi: VisualItem): Option[VisualEdge /* [S] */]
  // def getPorts(vi: VisualItem): Option[VisualPorts      ]

  def editObject(vi: VisualItem): Unit
  def connect(source: VisualBox[S], out: Port.Out, sink: VisualBox[S], in: Port.In): Unit
}