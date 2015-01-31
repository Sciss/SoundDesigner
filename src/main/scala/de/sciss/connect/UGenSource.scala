package de.sciss
package connect

import de.sciss.connect.impl.{UGenSourceImpl => Impl}
import de.sciss.lucre.event.Sys
import de.sciss.lucre.{event => evt}
import de.sciss.synth.{proc, UGenSpec}
import de.sciss.synth.proc.Elem

object UGenSource {
  def apply[S <: evt.Sys[S]](spec: UGenSpec)(implicit tx: S#Tx): UGenSource[S] = Impl(spec)

  implicit def serializer[S <: evt.Sys[S]]: serial.Serializer[S#Tx, S#Acc, UGenSource[S]] =
    Impl.serializer[S]

  object Obj {
    def unapply[S <: Sys[S]](obj: proc.Obj[S]): Option[Obj[S]] =
      if (obj.elem.isInstanceOf[UGenSource[S]]) Some(obj.asInstanceOf[Obj[S]])
      else None
  }
  type Obj[S <: Sys[S]] = proc.Obj.T[S, UGenSource]
}
trait UGenSource[S <: evt.Sys[S]] extends Elem[S] {
  //  type Peer = S#Var[Option[_UGenSource[_]]] // Expr.Var[S, String]
  //
  //  def spec: UGenSpec

  type Peer       = Unit
  type PeerUpdate = Unit  // XXX TODO

  def spec: UGenSpec

  def mkCopy()(implicit tx: S#Tx): UGenSource[S]
}