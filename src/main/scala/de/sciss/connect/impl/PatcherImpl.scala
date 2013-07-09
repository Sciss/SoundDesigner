package de.sciss.connect
package impl

import de.sciss.synth.proc.Sys
import de.sciss.serial
import de.sciss.lucre.{event => evt, data, expr}
import serial.{DataInput, DataOutput}
import expr.LinkedList

object PatcherImpl {
  // private type Map[S <: Sys[S]] = data.SkipList.Map[S, Int, List[Element[S]]]
  private type LL[S <: Sys[S]] = LinkedList.Modifiable[S, Element[S], Element.Update[S]]

  def apply[S <: Sys[S]](implicit tx: S#Tx, serializer: evt.Serializer[S, Element[S]]): Patcher[S] = {
    val targets = evt.Targets[S]
    // val map     = data.SkipList.Map.empty[S, Int, List[Element[S]]]
    val list = expr.LinkedList.Modifiable[S, Element[S], Element.Update[S]](_.changed)
    new Impl[S](targets, list)
  }

  def serializer[S <: Sys[S]](implicit elemSer: evt.Serializer[S, Element[S]]): evt.NodeSerializer[S, Patcher[S]] =
    new Ser[S]

  private final class Ser[S <: Sys[S]](implicit elemSer: evt.Serializer[S, Element[S]])
    extends evt.NodeSerializer[S, Patcher[S]] {

    def read(in: DataInput, access: S#Acc, targets: evt.Targets[S])(implicit tx: S#Tx): Patcher[S] = {
      val list = expr.LinkedList.Modifiable.read[S, Element[S], Element.Update[S]](_.changed)(in, access)
      new Impl[S](targets, list)
    }
  }

  private final class Impl[S <: Sys[S]](protected val targets: evt.Targets[S], list: LL[S])
                                       (implicit elemSer: evt.Serializer[S, Element[S]])
    extends Patcher[S] with evt.impl.StandaloneLike[S, Patcher.Update[S], Patcher[S]] {

    patcher =>

    def changed: evt.Event[S, Patcher.Update[S], Patcher[S]] = patcher

    protected def reader: evt.Reader[S, Patcher[S]] = serializer[S]

    def connect()(implicit tx: S#Tx) {
      list.changed ---> this
    }

    def disconnect()(implicit tx: S#Tx) {
      list.changed -/-> this
    }

    def iterator(implicit tx: S#Tx): data.Iterator[S#Tx, Element[S]] = list.iterator

    def add(elem: Element[S])(implicit tx: S#Tx) {
      list.addLast(elem)
    }

    def remove(elem: Element[S])(implicit tx: S#Tx): Boolean = list.remove(elem)

    def pullUpdate(pull: evt.Pull[S])(implicit tx: S#Tx): Option[Patcher.Update[S]] =
      pull(list.changed).map { ll =>
        val ch = ll.changes.map {
          case LinkedList.Added  (idx, elem)        => Patcher.Added  (elem)
          case LinkedList.Removed(idx, elem)        => Patcher.Removed(elem)
          case LinkedList.Element(elem, elemUpdate) => Patcher.Element(elem, elemUpdate)
        }
        Patcher.Update(patcher, ch)
      }

    protected def writeData(out: DataOutput) {
      list.write(out)
    }

    protected def disposeData()(implicit tx: S#Tx) {
      list.dispose()
    }
  }
}