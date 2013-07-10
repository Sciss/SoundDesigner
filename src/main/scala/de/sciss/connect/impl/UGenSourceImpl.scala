package de.sciss
package connect
package impl

import synth.expr.Strings
import de.sciss.lucre.{event => evt, stm, expr}
import serial.DataInput
import expr.Expr
import synth.proc.impl.AttributeImpl
import de.sciss.synth.{UGenSource => _UGenSource, UGenSpec}

object UGenSourceImpl extends AttributeImpl.Companion[UGenSource] {
  final val typeID = 18

  type _Peer[S <: evt.Sys[S]] = S#Var[Option[_UGenSource[_]]]

  def readIdentified[S <: evt.Sys[S]](in: DataInput, access: S#Acc, targets: evt.Targets[S])
                                     (implicit tx: S#Tx): UGenSource[S] with evt.Node[S] = {
    val spec: UGenSpec = ???
    val peer: _Peer[S] = ???
    new Impl(targets, spec, peer)
  }

  def apply[S <: evt.Sys[S]](spec: UGenSpec, init: Option[_UGenSource[_]])(implicit tx: S#Tx): UGenSource[S] = {
    val tgt   = evt.Targets[S]
    val peer  = ??? // tx.newVar(tgt.id, init)
    new Impl(tgt, spec, peer)
  }

  private final class Impl[S <: evt.Sys[S]](val targets: evt.Targets[S],
                                            val spec: UGenSpec, val peer: _Peer[S])
    extends AttributeImpl.Active[S] with UGenSource[S] {

    def typeID = UGenSourceImpl.typeID
    def prefix = "UGenSource"

    protected def peerEvent = evt.Dummy[S, Any, UGenSource[S]]

    def mkCopy()(implicit tx: S#Tx): UGenSource[S] = {
      ???
    }
  }
}
