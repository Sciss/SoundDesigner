package de.sciss.connect
package impl

import de.sciss.synth.proc.{Obj, Elem}
import de.sciss.serial
import de.sciss.lucre.{event => evt, data, expr}
import serial.{DataInput, DataOutput}
import de.sciss.lucre.synth.{InMemory, Sys}

object PatcherImpl {
  // private type Map[S <: Sys[S]] = data.SkipList.Map[S, Int, List[NodeChanged[S]]]
  private type LL[S <: Sys[S]] = expr.List.Modifiable[S, Obj[S], Obj.Update[S]]

  def apply[S <: Sys[S]](implicit tx: S#Tx): Patcher[S] = {
    val targets = evt.Targets[S]
    // val map     = data.SkipList.Map.empty[S, Int, List[NodeChanged[S]]]
    val list = expr.List.Modifiable[S, Obj[S], Obj.Update[S]]
    new Impl[S](targets, list)
  }

  def serializer[S <: Sys[S]]: evt.NodeSerializer[S, Patcher[S]] = anySer.asInstanceOf[Ser[S]]

  private val anySer = new Ser[InMemory]

  private final class Ser[S <: Sys[S]] extends evt.NodeSerializer[S, Patcher[S]] {
    def read(in: DataInput, access: S#Acc, targets: evt.Targets[S])(implicit tx: S#Tx): Patcher[S] = {
      val list = expr.List.Modifiable.read[S, Obj[S], Obj.Update[S]](in, access)
      new Impl[S](targets, list)
    }
  }

  private final class Impl[S <: Sys[S]](protected val targets: evt.Targets[S], list: LL[S])
    extends Patcher[S] with evt.impl.StandaloneLike[S, Patcher.Update[S], Patcher[S]] {

    patcher =>

    def changed: evt.Event[S, Patcher.Update[S], Patcher[S]] = patcher

    protected def reader: evt.Reader[S, Patcher[S]] = serializer[S]

    def connect   ()(implicit tx: S#Tx): Unit = list.changed ---> this
    def disconnect()(implicit tx: S#Tx): Unit = list.changed -/-> this

    def nodeIterator(implicit tx: S#Tx): data.Iterator[S#Tx, Obj[S]] = list.iterator

    def addNode   (elem: Obj[S])(implicit tx: S#Tx): Unit    = list.addLast(elem)
    def removeNode(elem: Obj[S])(implicit tx: S#Tx): Boolean = list.remove (elem)

    def pullUpdate(pull: evt.Pull[S])(implicit tx: S#Tx): Option[Patcher.Update[S]] =
      pull(list.changed).map { ll =>
        val ch = ll.changes.map {
          case expr.List.Added  (idx, elem)        => Patcher.NodeAdded  (elem)
          case expr.List.Removed(idx, elem)        => Patcher.NodeRemoved(elem)
          case expr.List.Element(elem, elemUpdate) => Patcher.NodeChanged(elem, elemUpdate)
        }
        Patcher.Update(patcher, ch)
      }

    protected def writeData(out: DataOutput): Unit = list.write(out)

    protected def disposeData()(implicit tx: S#Tx): Unit = list.dispose()
  }
}