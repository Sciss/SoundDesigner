package de.sciss.connect
package view

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
import impl.{PaneImpl => Impl}

object Pane {
  def apply[S <: Sys[S]](patcher: Patcher[S])(implicit tx: S#Tx, cursor: stm.Cursor[S]): Pane[S] = Impl[S](patcher)
}
trait Pane[S <: Sys[S]] {
  def component: Component
}