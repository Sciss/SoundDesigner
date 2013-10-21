package de.sciss.connect

import de.sciss.lucre.{event => evt}
import de.sciss.synth.proc.Attribute
import de.sciss.connect.impl.{ConnectionImpl => Impl}
import de.sciss.serial

object Connection {
  def apply[S <: evt.Sys[S]](source: (Attribute[S], Int), sink: (Attribute[S], Int))
                            (implicit tx: S#Tx): Connection[S] = Impl(source, sink)

  implicit def serializer[S <: evt.Sys[S]]: serial.Serializer[S#Tx, S#Acc, Connection[S]] =
    Impl.serializer[S]
}
trait Connection[S <: evt.Sys[S]] extends Attribute[S] {
  def source: (Attribute[S], Int)
  def sink  : (Attribute[S], Int)

  type Peer = Unit
}
