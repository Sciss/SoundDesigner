package de.sciss.connect
package view

import de.sciss.synth.proc.Sys
import prefuse.{Display, Visualization}
import javax.swing.JComponent
import java.awt.event.KeyEvent
import de.sciss.desktop.KeyStrokes
import scala.swing.Action
import prefuse.data.Graph
import de.sciss.lucre.stm

class Pane[S <: Sys[S]](implicit cursor: stm.Cursor[S]) {
  private final val GROUP_GRAPH   = "graph"
  private final val COL_ELEM      = "element"
  // private final val COL_PORTS     = "ports"

  val visualization = new Visualization
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
  g .addColumn(COL_ELEM , classOf[VisualElement[S]])
  private val vg  = visualization.addGraph(GROUP_GRAPH, g)
  // vg.addColumn(COL_PORTS, classOf[VisualPorts])

  private def putInt(): Unit = {
    //    val n       = g.addNode()
    //    val source  =
    //    n.set(COL_ELEM, new VisualInt[S](source, 0)
    //    val vi  = visualization.getVisualItem(GROUP_GRAPH, n)
    //    val mp  = dragControl.mousePoint
    //    vi.setX(mp.getX - 2)
    //    vi.setY(mp.getY - 2)
    //    editObject(vi)
    //    visualization.repaint()
  }
}