package de.sciss
package connect

import synth.proc.Attribute
import de.sciss.lucre.{event => evt}

import impl.{UGenSourceImpl => Impl}
import de.sciss.synth.UGenSpec

object UGenSource {
  def apply[S <: evt.Sys[S]](spec: UGenSpec)(implicit tx: S#Tx): UGenSource[S] = Impl(spec)

  implicit def serializer[S <: evt.Sys[S]]: serial.Serializer[S#Tx, S#Acc, UGenSource[S]] =
    Impl.serializer[S]
}
trait UGenSource[S <: evt.Sys[S]] extends Attribute[S] {
  //  type Peer = S#Var[Option[_UGenSource[_]]] // Expr.Var[S, String]
  //
  //  def spec: UGenSpec

  type Peer = UGenSource[S] // UGenSpec

  def spec: UGenSpec

  def mkCopy()(implicit tx: S#Tx): UGenSource[S]
}