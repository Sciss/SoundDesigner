package de.sciss
package connect

import de.sciss.synth.proc.{Attribute, Sys}
import de.sciss.lucre.{event => evt, data}
import evt.Event
import de.sciss.serial
import impl.{PatcherImpl => Impl}
import collection.immutable.{IndexedSeq => Vec}

object Patcher {
  // import connect.{Element => _Element}

  case class Update[S <: Sys[S]](patcher: Patcher[S], changes: Vec[Change[S]])
  sealed trait Change[S <: Sys[S]]
  case class Added  [S <: Sys[S]](elem: Attribute[S]) extends Change[S]
  case class Removed[S <: Sys[S]](elem: Attribute[S]) extends Change[S]
  case class Element[S <: Sys[S]](elem: Attribute[S], elemUpdate: Attribute.Update[S]) extends Change[S]

  implicit def serializer[S <: Sys[S]]: evt.NodeSerializer[S, Patcher[S]] = Impl.serializer[S]

  def apply[S <: Sys[S]](implicit tx: S#Tx): Patcher[S] = Impl[S]
}
trait Patcher[S <: Sys[S]] extends evt.Node[S] {
  def iterator(implicit tx: S#Tx): data.Iterator[S#Tx, Attribute[S]]

  def add   (elem: Attribute[S])(implicit tx: S#Tx): Unit
  def remove(elem: Attribute[S])(implicit tx: S#Tx): Boolean

  def changed: Event[S, Patcher.Update[S], Patcher[S]]
}