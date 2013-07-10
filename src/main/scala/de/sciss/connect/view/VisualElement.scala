package de.sciss.connect
package view

import de.sciss.lucre.stm
import de.sciss.synth.proc.{Attribute, Sys}

sealed trait VisualElement[S <: Sys[S]] {
  //  var name: String = ""
  var state: ElementState = ElementState.Ok
}
//object VisualProduct {
//  def unapply[S <: Sys[S]](p: VisualProduct[S]): Option[stm.Source[S#Tx, Product[S]]] = p.content
//}
class VisualProduct[S <: Sys[S]](val source: stm.Source[S#Tx, Product[S]], var value: Any)
  extends VisualElement[S]

class VisualInvalidObject[S <: Sys[S]](var text: String) extends VisualElement[S] {
  override var state: ElementState = ElementState.Edit
}

class VisualInt[S <: Sys[S]](val source: stm.Source[S#Tx, Attribute.Int[S]], var value: Int)
  extends VisualElement[S]

class VisualBoolean[S <: Sys[S]](val source: stm.Source[S#Tx, Attribute.Boolean[S]], var value: Boolean)
  extends VisualElement[S]