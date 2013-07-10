package de.sciss
package connect
package impl

import synth.expr.Strings
import lucre.{event => evt, expr}
import serial.DataInput
import expr.Expr
import synth.proc.impl.AttributeImpl

object IncompleteProductImpl extends AttributeImpl.Companion[IncompleteProduct] {
  final val typeID = 17

  def readIdentified[S <: evt.Sys[S]](in: DataInput, access: S#Acc, targets: evt.Targets[S])
                                     (implicit tx: S#Tx): IncompleteProduct[S] with evt.Node[S] = {
    val peer = Strings.readVar(in, access)
    new Impl(targets, peer)
  }

  def apply[S <: evt.Sys[S]](init: String)(implicit tx: S#Tx): IncompleteProduct[S] =
    apply1[S](Strings.newVar(Strings.newConst(init)))

  private def apply1[S <: evt.Sys[S]](peer: Expr.Var[S, String])(implicit tx: S#Tx): IncompleteProduct[S] =
    new Impl(evt.Targets[S], peer)

  private final class Impl[S <: evt.Sys[S]](val targets: evt.Targets[S], val peer: Expr.Var[S, String])
    extends AttributeImpl.Expr[S, String] with IncompleteProduct[S] {

    def typeID = IncompleteProductImpl.typeID
    def prefix = "IncompleteProduct"

    def mkCopy()(implicit tx: S#Tx): IncompleteProduct[S] = {
      val newPeer = Strings.newVar(peer())
      apply1(newPeer)
    }
  }
}
