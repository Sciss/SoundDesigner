package de.sciss
package connect
package impl

import de.sciss.lucre.{event => evt}
import de.sciss.serial.{DataOutput, DataInput}
import synth.proc.impl.AttributeImpl
import de.sciss.synth.proc.Attribute
import de.sciss.synth.proc.Attribute.Update

object ConnectionImpl extends AttributeImpl.Companion[Connection] {
  final val typeID = 19

  def readIdentified[S <: evt.Sys[S]](in: DataInput, access: S#Acc, targets: evt.Targets[S])
                                     (implicit tx: S#Tx): Connection[S] with evt.Node[S] = {
    // val spec: UGenSpec = UGenSpecSerializer.read(in)
    val sourceAttr  = Attribute.serializer.read(in, access)
    val sourceLet   = in.readInt()
    val sinkAttr    = Attribute.serializer.read(in, access)
    val sinkLet     = in.readInt()

    new Impl(targets, (sourceAttr, sourceLet), (sinkAttr, sinkLet))
  }

  def apply[S <: evt.Sys[S]](source: (Attribute[S], Int), sink: (Attribute[S], Int))
                            (implicit tx: S#Tx): Connection[S] = {
    val tgt   = evt.Targets[S]
    new Impl(tgt, source, sink)
  }

  private final class Impl[S <: evt.Sys[S]](val targets: evt.Targets[S],
                                            val source: (Attribute[S], Int), val sink: (Attribute[S], Int))
    extends Attribute[S] with evt.Node[S] with Connection[S] {

    def typeID = ConnectionImpl.typeID
    def prefix = "Connection"

    def peer = this

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

    def changed = evt.Dummy[S, Update[S]]
  }
}
