package de.sciss
package connect
package impl

import de.sciss.lucre.event.Sys
import de.sciss.lucre.{event => evt}
import de.sciss.serial.{DataInput, DataOutput}
import de.sciss.synth.proc.Elem.Update
import de.sciss.synth.proc.impl.ElemCompanionImpl
import de.sciss.synth.proc.{Elem, Obj}

object ConnectionImpl extends ElemCompanionImpl[Connection] {
  final val typeID = 19

  def readIdentifiedConstant[S <: Sys[S]](in: DataInput)(implicit tx: S#Tx): Connection[S] = ???

  def readIdentified[S <: evt.Sys[S]](in: DataInput, access: S#Acc, targets: evt.Targets[S])
                                     (implicit tx: S#Tx): Connection[S] with evt.Node[S] = {
    // val spec: UGenSpec = UGenSpecSerializer.read(in)
    val sourceAttr  = Obj.serializer.read(in, access)
    val sourceLet   = in.readInt()
    val sinkAttr    = Obj.serializer.read(in, access)
    val sinkLet     = in.readInt()

    new Impl(targets, (sourceAttr, sourceLet), (sinkAttr, sinkLet))
  }

  def apply[S <: evt.Sys[S]](source: (Obj[S], Int), sink: (Obj[S], Int))
                            (implicit tx: S#Tx): Connection[S] = {
    val tgt   = evt.Targets[S]
    new Impl(tgt, source, sink)
  }

  private final class Impl[S <: evt.Sys[S]](val targets: evt.Targets[S],
                                            val source: (Obj[S], Int), val sink: (Obj[S], Int))
    extends Elem[S] with evt.Node[S] with Connection[S] {

    def typeID = ConnectionImpl.typeID
    def prefix = "Connection"

    val peer = () // this

    // protected def peerEvent = evt.Dummy[S, Any, Connection[S]]

    def mkCopy()(implicit tx: S#Tx): Connection[S] = apply(source, sink)

    protected def writeData(out: DataOutput): Unit = {
      out.writeInt(typeID)
      source._1.write(out)
      out.writeInt(source._2)
      sink  ._1.write(out)
      out.writeInt(sink  ._2)
    }

    protected def disposeData()(implicit tx: S#Tx) = ()

    def select(slot: Int) = sys.error("Not an actual Node") // XXX TODO ugly

    def changed = evt.Dummy[S, Update[S, Unit]]
  }
}
