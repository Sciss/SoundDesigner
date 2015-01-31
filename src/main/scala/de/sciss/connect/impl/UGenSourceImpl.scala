package de.sciss
package connect
package impl

import de.sciss.lucre.event.Sys
import de.sciss.lucre.{event => evt}
import de.sciss.serial.{DataInput, DataOutput}
import de.sciss.synth.UGenSpec
import de.sciss.synth.proc.Elem
import de.sciss.synth.proc.Elem.Update
import de.sciss.synth.proc.impl.ElemCompanionImpl

object UGenSourceImpl extends ElemCompanionImpl[UGenSource] {
  final val typeID = 18

  // type _Peer[S <: evt.Sys[S]] = S#Var[Option[_UGenSource[_]]]

  def readIdentifiedConstant[S <: Sys[S]](in: DataInput)(implicit tx: S#Tx): UGenSource[S] = ???

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
                                            val spec: UGenSpec)
    extends Elem[S] with evt.Node[S] with UGenSource[S] {

    def typeID = UGenSourceImpl.typeID
    def prefix = "UGenSource"

    val peer = ()

    // protected def peerEvent = evt.Dummy[S, Any, UGenSource[S]]

    def mkCopy()(implicit tx: S#Tx): UGenSource[S] = apply(spec)

    protected def writeData(out: DataOutput): Unit = {
      out.writeInt(typeID)
      UGenSpecSerializer.write(spec, out)
    }

    protected def disposeData()(implicit tx: S#Tx) = ()

    def select(slot: Int) = sys.error("Not an actual Node") // XXX TODO ugly

    def changed = evt.Dummy[S, Update[S, Unit]]
  }
}
