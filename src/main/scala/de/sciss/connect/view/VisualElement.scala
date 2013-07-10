package de.sciss.connect
package view

import de.sciss.lucre.stm
import de.sciss.synth.proc.{Attribute, Sys}
import de.sciss.connect.view.impl.IntRenderer

sealed trait VisualElement /* [S <: Sys[S]] */ {
  //  var name: String = ""
  protected def defaultState: ElementState = ElementState.Ok

  var state: ElementState = defaultState

  def renderer: ElementRenderer
}

sealed trait VisualElementT[S <: Sys[S]] extends VisualElement

//object VisualProduct {
//  def unapply[S <: Sys[S]](p: VisualProduct[S]): Option[stm.Source[S#Tx, Product[S]]] = p.content
//}
class VisualProduct[S <: Sys[S]](val source: stm.Source[S#Tx, Product[S]], var value: Any)
  extends VisualElementT[S] {

  def renderer: ElementRenderer = ???
}

class VisualInvalidObject[S <: Sys[S]](var text: String) extends VisualElementT[S] {
  override def defaultState = ElementState.Edit

  def renderer: ElementRenderer = ???
}

class VisualInt[S <: Sys[S]](val source: stm.Source[S#Tx, Attribute.Int[S]], var value: Int)
  extends VisualElementT[S] {

  def renderer: ElementRenderer = IntRenderer
}

class VisualBoolean[S <: Sys[S]](val source: stm.Source[S#Tx, Attribute.Boolean[S]], var value: Boolean)
  extends VisualElementT[S] {

  def renderer: ElementRenderer = ???
}