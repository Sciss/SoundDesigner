package de.sciss
package connect
package impl

import de.sciss.lucre.{event => evt}
import de.sciss.serial.{DataOutput, DataInput}
import synth.proc.impl.AttributeImpl
import de.sciss.synth.UGenSpec
import de.sciss.synth.proc.Attribute
import de.sciss.synth.proc.Attribute.Update

object UGenSourceImpl extends AttributeImpl.Companion[UGenSource] {
  final val typeID = 18

  // type _Peer[S <: evt.Sys[S]] = S#Var[Option[_UGenSource[_]]]

  def readIdentified[S <: evt.Sys[S]](in: DataInput, access: S#Acc, targets: evt.Targets[S])
                                     (implicit tx: S#Tx): UGenSource[S] with evt.Node[S] = {
    val spec: UGenSpec = UGenSpecSerializer.read(in)
    new Impl(targets, spec)
  }

  def apply[S <: evt.Sys[S]](spec: UGenSpec)(implicit tx: S#Tx): UGenSource[S] = {
    val tgt   = evt.Targets[S]
    new Impl(tgt, spec)
  }

  private final class Impl[S <: evt.Sys[S]](val targets: evt.Targets[S],
                                            val peer: UGenSpec)
    extends Attribute[S] with evt.Node[S] with UGenSource[S] {

    def typeID = UGenSourceImpl.typeID
    def prefix = "UGenSource"

    // protected def peerEvent = evt.Dummy[S, Any, UGenSource[S]]

    def mkCopy()(implicit tx: S#Tx): UGenSource[S] = {
      apply(peer)
    }

    protected def writeData(out: DataOutput): Unit = {
      out.writeInt(typeID)
      UGenSpecSerializer.write(peer, out)
    }

    protected def disposeData()(implicit tx: S#Tx) = ()

    def select(slot: Int) = sys.error("Not an actual Node") // XXX TODO ugly

    def changed = evt.Dummy[S, Update[S], Attribute[S]]
  }
}
