package de.sciss
package connect
package impl

import de.sciss.lucre.event.Sys
import lucre.{event => evt, expr}
import serial.DataInput
import expr.Expr
import de.sciss.synth.proc.impl.{ActiveElemImpl, PassiveElemImpl, ElemCompanionImpl, ElemImpl}
import de.sciss.lucre.expr.{String => StringEx}

object IncompleteElementImpl extends ElemCompanionImpl[IncompleteElement] {
  final val typeID = 17

  def readIdentifiedConstant[S <: Sys[S]](in: DataInput)(implicit tx: S#Tx): IncompleteElement[S] = ???

  def readIdentified[S <: evt.Sys[S]](in: DataInput, access: S#Acc, targets: evt.Targets[S])
                                     (implicit tx: S#Tx): IncompleteElement[S] with evt.Node[S] = {
    val peer = StringEx.readVar(in, access)
    new Impl(targets, peer)
  }

  def apply[S <: evt.Sys[S]](init: String)(implicit tx: S#Tx): IncompleteElement[S] =
    apply1[S](StringEx.newVar(StringEx.newConst(init)))

  private def apply1[S <: evt.Sys[S]](peer: Expr.Var[S, String])(implicit tx: S#Tx): IncompleteElement[S] =
    new Impl(evt.Targets[S], peer)

  private final class Impl[S <: evt.Sys[S]](val targets: evt.Targets[S], val peer: Expr.Var[S, String])
    extends ActiveElemImpl[S] with IncompleteElement[S] {

    def typeID = IncompleteElementImpl.typeID
    def prefix = "IncompleteElement"

    def mkCopy()(implicit tx: S#Tx): IncompleteElement[S] = {
      val newPeer = StringEx.newVar(peer())
      apply1(newPeer)
    }
  }
}
