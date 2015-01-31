package de.sciss
package connect

import de.sciss.connect.impl.{IncompleteElementImpl => Impl}
import de.sciss.lucre.event.Sys
import de.sciss.lucre.expr.Expr
import de.sciss.lucre.{event => evt}
import de.sciss.model.Change
import de.sciss.synth.proc
import de.sciss.synth.proc.Elem

object IncompleteElement {
  def apply[S <: evt.Sys[S]](init: String = "")(implicit tx: S#Tx): IncompleteElement[S] = Impl(init)

  implicit def serializer[S <: evt.Sys[S]]: serial.Serializer[S#Tx, S#Acc, IncompleteElement[S]] =
    Impl.serializer[S]

  object Obj {
    def unapply[S <: Sys[S]](obj: proc.Obj[S]): Option[Obj[S]] =
      if (obj.elem.isInstanceOf[IncompleteElement[S]]) Some(obj.asInstanceOf[Obj[S]])
      else None
  }
  type Obj[S <: Sys[S]] = proc.Obj.T[S, IncompleteElement]
}
trait IncompleteElement[S <: evt.Sys[S]] extends Elem[S] {
  type Peer       = Expr.Var[S, String]
  type PeerUpdate = Change[String]

  def mkCopy()(implicit tx: S#Tx): IncompleteElement[S]
}