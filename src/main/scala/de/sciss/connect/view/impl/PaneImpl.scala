package de.sciss.connect
package view
package impl

import de.sciss.synth.proc.{Attribute, Sys}
import prefuse.{Display, Visualization}
import javax.swing.{JTextField, JComponent}
import java.awt.event.{ActionListener, ActionEvent, KeyEvent}
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
import java.awt.Rectangle
import javax.swing.event.{DocumentListener, DocumentEvent}

// XXX TODO: requires call on EDT
object PaneImpl {
  def apply[S <: Sys[S]](patcher: Patcher[S], config: Pane.Config[S])
                        (implicit tx: S#Tx, cursor: stm.Cursor[S]): PaneImpl[S] = {
    val cueMap = tx.newInMemoryIDMap[PutMetaData]
    val res = new Impl[S](tx.newHandle(patcher), cueMap, config)
    patcher.changed.react { implicit tx => { upd =>
      upd.changes.foreach {
        case Patcher.Added  (elem) => res.elemAdded  (elem)
        case Patcher.Removed(elem) => res.elemRemoved(elem)
        case Patcher.Element(elem, elemUpd) =>
      }
    }}

    // patcher.changed.react(_ => upd => println(s"Observed update $upd"))

    res
  }

  private case class PutMetaData(point: Point2D, edit: Boolean)

  private final val GROUP_GRAPH   = "graph"
  private final val COL_ELEM      = "element"
  // private final val COL_PORTS     = "ports"

  private final class Impl[S <: Sys[S]](patcher: stm.Source[S#Tx, Patcher[S]],
                                        cueMap: IdentifierMap[S#ID, S#Tx, PutMetaData],
                                        config: Pane.Config[S])
                                       (implicit cursor: stm.Cursor[S]) extends PaneImpl[S] {
    val imp = ExprImplicits[S]
    import imp._

    val visualization = new Visualization
    private val rf = new DefaultRendererFactory(new BoxRenderer(this))
    visualization.setRendererFactory(rf)

    private var editingNode     = Option.empty[VisualItem]
    private var editingOldText  = ""

    val dragControl   = new DragControl(this)
    val display  = {
      val res   = new Display(visualization)
      val imap  = res.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
      val amap  = res.getActionMap
      import KeyEvent._
      import KeyStrokes._
      imap.put(menu1 + VK_1, "designer.putObject")
      amap.put("designer.putObject", Action("putObject") {
        put(edit = true) { implicit tx => IncompleteElement[S]() }
      }.peer)
      imap.put(menu1 + VK_3, "designer.putInt")
      amap.put("designer.putInt", Action("putInt") {
        put() { implicit tx => Attribute.Int[S](0) }
      }.peer)
      res
    }
    private val g   = new Graph
    g .addColumn(COL_ELEM , classOf[VisualElementT[S]])
    /* private val vg = */ visualization.addGraph(GROUP_GRAPH, g)
    // vg.addColumn(COL_PORTS, classOf[VisualPorts])

    display.addControlListener(dragControl)
    display.addControlListener(new PanControl())
    display.addControlListener(new ZoomControl())

    private def lastMousePoint(): Point2D = dragControl.mousePoint

    private def put(edit: Boolean = false)(elem: S#Tx => Attribute[S]): Unit = {
      val mp  = lastMousePoint()
      val cue = PutMetaData(mp, edit)
      cursor.step { implicit tx =>
        val e = elem(tx)
        // println(s"Put cue $cue")
        cueMap.put(e.id, cue)
        patcher().add(e)
      }
    }

    def elemAdded(elem: Attribute[S])(implicit tx: S#Tx): Unit = {
      val dataOpt = elem match {
        case a: Attribute.Int[S]      => Some(new VisualInt              [S](tx.newHandle(a), a.peer.value))
        case a: IncompleteElement[S]  => Some(new VisualIncompleteElement[S](tx.newHandle(a), a.peer.value))
        case _ => None
      }
      dataOpt.foreach { data =>
        val cueOpt = cueMap.get(elem.id)
        if (cueOpt.nonEmpty) cueMap.remove(elem.id)
        // println(s"Get cue $cueOpt")
        guiFromTx {
          val n       = g.addNode()
          n.set(COL_ELEM, data)
          val vi      = visualization.getVisualItem(GROUP_GRAPH, n)
          val mp      = cueOpt.map(_.point).getOrElse(lastMousePoint())
          vi.setX(mp.getX - 2)
          vi.setY(mp.getY - 2)
          // val ports = new VisualPorts(numIns = 0, numOuts = 1)
          data.ports.update(vi.getBounds)
          // vi.set(COL_PORTS, ports)
          if (cueOpt.exists(_.edit)) editObject(vi)
          visualization.repaint()
        }
      }
    }

    def elemRemoved(elem: Attribute[S])(implicit tx: S#Tx): Unit = {
      println("TODO: remove")
    }

    val component = Component.wrap(display)

    def getData (vi: VisualItem): Option[VisualElementT[S]] = Option(vi.get(COL_ELEM ).asInstanceOf[VisualElementT[S]])
    // def getPorts(vi: VisualItem) = Option(vi.get(COL_PORTS).asInstanceOf[VisualPorts      ])

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
      getData(vi).foreach(_.ports.update(b))
      r
    }

    def editObject(vi: VisualItem): Unit = getData(vi).foreach {
      case data: VisualIncompleteElement[S] =>
        val r = updateEditingBounds(vi)
        editingNode     = Some(vi)
        editingOldText  = data.value
        display.editText(data.value, r)
      case _ =>
    }

    private def stopEditing(): Unit = {
      val txt = display.getTextEditor.getText
      display.stopEditing()
      editingNode.foreach { vi =>
        editingNode = None
        getData(vi).foreach { data =>
          // data.name = txt
          data match {
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
                    p.remove(incomplete)
                    cueMap.put(complete.id, cue)
                    p.add(complete)
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
    }

    display.getTextEditor match {
      case tf: JTextField =>
        tf.setFont(Style.font)
        tf.setForeground(Style.selectionColor)
        tf.setBackground(Style.boxColor)
        tf.getDocument.addDocumentListener(new DocumentListener {
          def refreshBox() {
            editingNode.foreach { vi =>
              getData(vi).foreach {
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
          }

          def insertUpdate( e: DocumentEvent) { refreshBox() }
          def removeUpdate( e: DocumentEvent) { refreshBox() }
          def changedUpdate(e: DocumentEvent) { refreshBox() }
        })
        tf.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent) {
            stopEditing()
          }
      })
    }
  }
}
sealed trait PaneImpl[S <: Sys[S]] extends Pane[S] {
  def visualization: Visualization
  def display      : Display
  def getData (vi: VisualItem): Option[VisualElementT[S]]
  // def getPorts(vi: VisualItem): Option[VisualPorts      ]

  def editObject(vi: VisualItem): Unit
}