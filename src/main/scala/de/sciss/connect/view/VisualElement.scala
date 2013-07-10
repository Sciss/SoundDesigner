package de.sciss.connect
package view

import de.sciss.lucre.stm
import de.sciss.synth.proc.{Attribute, Sys}
import de.sciss.connect.view.impl.StringRenderer

sealed trait VisualElement /* [S <: Sys[S]] */ {
  //  var name: String = ""
  protected def defaultState: ElementState = ElementState.Ok

  var state: ElementState = defaultState

  def renderer: ElementRenderer

  def ports: VisualPorts

  def value: Any
}

sealed trait VisualElementT[S <: Sys[S]] extends VisualElement

//object VisualProduct {
//  def unapply[S <: Sys[S]](p: VisualProduct[S]): Option[stm.Source[S#Tx, Product[S]]] = p.content
//}
class VisualProduct[S <: Sys[S]](val source: stm.Source[S#Tx, Product[S]], var value: Any)
  extends VisualElementT[S] {

  def ports = ???

  def renderer: ElementRenderer = ???
}

class VisualIncompleteProduct[S <: Sys[S]](val source: stm.Source[S#Tx, IncompleteProduct[S]], var value: String)
  extends VisualElementT[S] {

  override def defaultState = ElementState.Edit

  val ports = new VisualPorts(0, 0)

  def renderer: ElementRenderer = StringRenderer
}

class VisualInt[S <: Sys[S]](val source: stm.Source[S#Tx, Attribute.Int[S]], var value: Int)
  extends VisualElementT[S] {

  val ports = new VisualPorts(numIns = 0, numOuts = 1)

  def renderer: ElementRenderer = StringRenderer
}

class VisualBoolean[S <: Sys[S]](val source: stm.Source[S#Tx, Attribute.Boolean[S]], var value: Boolean)
  extends VisualElementT[S] {

  val ports = new VisualPorts(numIns = 0, numOuts = 1)

  def renderer: ElementRenderer = ???
}