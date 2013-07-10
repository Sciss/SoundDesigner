package de.sciss
package connect

import synth.proc.{Sys, Attribute}
import lucre.{event => evt, expr}
import expr.Expr

import impl.{IncompleteProductImpl => Impl}

object IncompleteProduct {
  def apply[S <: evt.Sys[S]](init: String = "")(implicit tx: S#Tx): IncompleteProduct[S] = Impl(init)

  implicit def serializer[S <: evt.Sys[S]]: serial.Serializer[S#Tx, S#Acc, IncompleteProduct[S]] =
    Impl.serializer[S]
}
trait IncompleteProduct[S <: evt.Sys[S]] extends Attribute[S] {
  type Peer = Expr.Var[S, String]
  def mkCopy()(implicit tx: S#Tx): IncompleteProduct[S]
}