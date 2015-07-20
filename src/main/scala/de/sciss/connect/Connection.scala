package de.sciss.connect

import de.sciss.connect.impl.{ConnectionImpl => Impl}
import de.sciss.lucre.event.Sys
import de.sciss.lucre.{event => evt}
import de.sciss.serial
import de.sciss.synth.proc
import de.sciss.synth.proc.{Obj, Elem}

object Connection {
  def apply[S <: evt.Sys[S]](source: (proc.Obj[S], Int), sink: (proc.Obj[S], Int))
                            (implicit tx: S#Tx): Connection[S] = Impl(source, sink)

  implicit def serializer[S <: evt.Sys[S]]: serial.Serializer[S#Tx, S#Acc, Connection[S]] =
    Impl.serializer[S]

  object Obj {
    def unapply[S <: Sys[S]](obj: proc.Obj[S]): Option[Obj[S]] =
      if (obj.elem.isInstanceOf[Connection[S]]) Some(obj.asInstanceOf[Obj[S]])
      else None
  }
  type Obj[S <: Sys[S]] = proc.Obj.T[S, Connection]
}
trait Connection[S <: evt.Sys[S]] extends Elem[S] {
  def source: (Obj[S], Int)
  def sink  : (Obj[S], Int)

  type Peer       = Unit
  type PeerUpdate = Unit  // XXX  TODO

  type This = Connection[S]
}
