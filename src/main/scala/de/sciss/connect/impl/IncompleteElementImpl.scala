package de.sciss
package connect
package impl

import lucre.{event => evt, expr}
import serial.DataInput
import expr.Expr
import synth.proc.impl.AttributeImpl
import de.sciss.lucre.synth.expr.Strings

object IncompleteElementImpl extends AttributeImpl.Companion[IncompleteElement] {
  final val typeID = 17

  def readIdentified[S <: evt.Sys[S]](in: DataInput, access: S#Acc, targets: evt.Targets[S])
                                     (implicit tx: S#Tx): IncompleteElement[S] with evt.Node[S] = {
    val peer = Strings.readVar(in, access)
    new Impl(targets, peer)
  }

  def apply[S <: evt.Sys[S]](init: String)(implicit tx: S#Tx): IncompleteElement[S] =
    apply1[S](Strings.newVar(Strings.newConst(init)))

  private def apply1[S <: evt.Sys[S]](peer: Expr.Var[S, String])(implicit tx: S#Tx): IncompleteElement[S] =
    new Impl(evt.Targets[S], peer)

  private final class Impl[S <: evt.Sys[S]](val targets: evt.Targets[S], val peer: Expr.Var[S, String])
    extends AttributeImpl.Expr[S, String] with IncompleteElement[S] {

    def typeID = IncompleteElementImpl.typeID
    def prefix = "IncompleteElement"

    def mkCopy()(implicit tx: S#Tx): IncompleteElement[S] = {
      val newPeer = Strings.newVar(peer())
      apply1(newPeer)
    }
  }
}
