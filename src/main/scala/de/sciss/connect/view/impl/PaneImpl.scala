package de.sciss.connect
package view
package impl

import de.sciss.synth.proc.{Attribute, Sys}
import prefuse.{Display, Visualization}
import javax.swing.JComponent
import java.awt.event.KeyEvent
import de.sciss.desktop.KeyStrokes
import scala.swing.{Component, Action}
import prefuse.data.Graph
import de.sciss.lucre.stm
import de.sciss.synth.expr.ExprImplicits
import de.sciss.lucre.stm.IdentifierMap
import java.awt.geom.Point2D
import prefuse.visual.VisualItem
import prefuse.render.DefaultRendererFactory
import prefuse.controls.{ZoomControl, PanControl}

// XXX TODO: requires call on EDT
object PaneImpl {
  def apply[S <: Sys[S]](patcher: Patcher[S])(implicit tx: S#Tx, cursor: stm.Cursor[S]): PaneImpl[S] = {
    val cueMap = tx.newInMemoryIDMap[ClickCue]
    val res = new Impl[S](tx.newHandle(patcher), cueMap)
    patcher.changed.react { implicit tx => { upd =>
      upd.changes.foreach {
        case Patcher.Added  (elem) => res.elemAdded(elem)
        case Patcher.Removed(elem) =>
        case Patcher.Element(elem, elemUpd) =>
      }
    }}

    // patcher.changed.react(_ => upd => println(s"Observed update $upd"))

    res
  }

  private case class ClickCue(point: Point2D)

  private final val GROUP_GRAPH   = "graph"
  private final val COL_ELEM      = "element"

  private final class Impl[S <: Sys[S]](patcher: stm.Source[S#Tx, Patcher[S]],
                                        cueMap: IdentifierMap[S#ID, S#Tx, ClickCue])
                                       (implicit cursor: stm.Cursor[S]) extends PaneImpl[S] {

    val visualization = new Visualization
    private val rf = new DefaultRendererFactory(new BoxRenderer(this))
    visualization.setRendererFactory(rf)
    val dragControl   = new DragControl(this)
    val display  = {
      val res   = new Display(visualization)
      val imap  = res.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
      val amap  = res.getActionMap
      import KeyEvent._
      import KeyStrokes._
      imap.put(menu1 + VK_1, "designer.putObject")
      amap.put("designer.putObject", Action("putObject") {
        //enterPutObject()
      }.peer)
      imap.put(menu1 + VK_3, "designer.putInt")
      amap.put("designer.putInt", Action("putInt") {
        putInt()
      }.peer)
      res
    }
    private val g   = new Graph
    g .addColumn(COL_ELEM , classOf[VisualElementT[S]])
    private val vg  = visualization.addGraph(GROUP_GRAPH, g)
    // vg.addColumn(COL_PORTS, classOf[VisualPorts])

    display.addControlListener(dragControl)
    display.addControlListener(new PanControl())
    display.addControlListener(new ZoomControl())

    private def lastMousePoint(): Point2D = dragControl.mousePoint

    private def putInt(): Unit = {
      val imp = ExprImplicits[S]
      import imp._
      val mp  = lastMousePoint()
      val cue = ClickCue(mp)
      cursor.step { implicit tx =>
        val elem = Attribute.Int[S](0)
        // println(s"Put cue $cue")
        cueMap.put(elem.id, cue)
        patcher().add(elem)
      }

      //    val n       = g.addNode()
      //    val source  =
      //    n.set(COL_ELEM, new VisualInt[S](source, 0)
      //    val vi  = visualization.getVisualItem(GROUP_GRAPH, n)
      //    vi.setX(mp.getX - 2)
      //    vi.setY(mp.getY - 2)
      //    editObject(vi)
      //    visualization.repaint()
    }

    def elemAdded(elem: Attribute[S])(implicit tx: S#Tx): Unit = {
      val vidOpt = elem match {
        case i: Attribute.Int[S] => Some(new VisualInt[S](tx.newHandle(i), i.peer.value))
        case _ => None
      }
      vidOpt.foreach { vid =>
        val cueOpt = cueMap.get(elem.id)
        // println(s"Get cue $cueOpt")
        guiFromTx {
          val n       = g.addNode()
          n.set(COL_ELEM, vid)
          val vi      = visualization.getVisualItem(GROUP_GRAPH, n)
          val mp      = cueOpt.map(_.point).getOrElse(lastMousePoint())
          vi.setX(mp.getX - 2)
          vi.setY(mp.getY - 2)
          // editObject(vi)
          visualization.repaint()
        }
      }
    }

    val component = Component.wrap(display)

    def getData(vi: VisualItem) = Option(vi.get(COL_ELEM).asInstanceOf[VisualElementT[S]])
  }
}
sealed trait PaneImpl[S <: Sys[S]] extends Pane[S] {
  def visualization: Visualization
  def display      : Display
  def getData(vi: VisualItem): Option[VisualElementT[S]]
}