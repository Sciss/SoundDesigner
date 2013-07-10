package de.sciss
package connect

import synth.proc.Attribute
import lucre.{event => evt, expr}
import expr.Expr

import impl.{IncompleteElementImpl => Impl}

object IncompleteElement {
  def apply[S <: evt.Sys[S]](init: String = "")(implicit tx: S#Tx): IncompleteElement[S] = Impl(init)

  implicit def serializer[S <: evt.Sys[S]]: serial.Serializer[S#Tx, S#Acc, IncompleteElement[S]] =
    Impl.serializer[S]
}
trait IncompleteElement[S <: evt.Sys[S]] extends Attribute[S] {
  type Peer = Expr.Var[S, String]
  def mkCopy()(implicit tx: S#Tx): IncompleteElement[S]
}