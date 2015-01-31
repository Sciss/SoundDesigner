package de.sciss
package connect

import de.sciss.connect.impl.{PatcherImpl => Impl}
import de.sciss.lucre.event.Event
import de.sciss.lucre.synth.Sys
import de.sciss.lucre.{data, event => evt}
import de.sciss.synth.proc.Obj

import scala.collection.immutable.{IndexedSeq => Vec}
import scala.language.existentials

object Patcher {
  // import connect.{NodeChanged => _Element}

  case class Update[S <: Sys[S]](patcher: Patcher[S], changes: Vec[Change[S]])
  sealed trait Change[S <: Sys[S]]
  case class NodeAdded  [S <: Sys[S]](elem: Obj[S]) extends Change[S]
  case class NodeRemoved[S <: Sys[S]](elem: Obj[S]) extends Change[S]
  case class NodeChanged[S <: Sys[S]](elem: Obj[S], elemUpdate: Obj.Update[S]) extends Change[S]

  implicit def serializer[S <: Sys[S]]: evt.NodeSerializer[S, Patcher[S]] = Impl.serializer[S]

  def apply[S <: Sys[S]](implicit tx: S#Tx): Patcher[S] = Impl[S]
}
trait Patcher[S <: Sys[S]] extends evt.Node[S] {
  def nodeIterator(implicit tx: S#Tx): data.Iterator[S#Tx, Obj[S]]

  def addNode   (elem: Obj[S])(implicit tx: S#Tx): Unit
  def removeNode(elem: Obj[S])(implicit tx: S#Tx): Boolean

  def changed: Event[S, Patcher.Update[S], Patcher[S]]
}