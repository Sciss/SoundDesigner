package de.sciss
package connect

import de.sciss.synth.proc.Sys
import de.sciss.lucre.{event => evt, data}
import evt.Event
import de.sciss.serial
import impl.{PatcherImpl => Impl}
import collection.immutable.{IndexedSeq => Vec}

object Patcher {
  import connect.{Element => _Element}

  case class Update[S <: Sys[S]](patcher: Patcher[S], changes: Vec[Change[S]])
  sealed trait Change[S <: Sys[S]]
  case class Added  [S <: Sys[S]](elem: _Element[S]) extends Change[S]
  case class Removed[S <: Sys[S]](elem: _Element[S]) extends Change[S]
  case class Element[S <: Sys[S]](elem: _Element[S], elemUpdate: _Element.Update[S]) extends Change[S]

  def apply[S <: Sys[S]](implicit tx: S#Tx, serializer: evt.Serializer[S, _Element[S]]): Patcher[S] = Impl[S]
}
trait Patcher[S <: Sys[S]] extends evt.Node[S] {
  def iterator(implicit tx: S#Tx): data.Iterator[S#Tx, Element[S]]

  def add   (elem: Element[S])(implicit tx: S#Tx): Unit
  def remove(elem: Element[S])(implicit tx: S#Tx): Boolean

  def changed: Event[S, Patcher.Update[S], Patcher[S]]
}