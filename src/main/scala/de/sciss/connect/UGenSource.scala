package de.sciss
package connect

import synth.proc.Attribute
import de.sciss.lucre.{event => evt, stm, expr}
import expr.Expr

// import impl.{UGenImpl => Impl}
import de.sciss.synth.{UGenSource => _UGenSource, UGenSpec, GE}

object UGenSource {
  def apply[S <: evt.Sys[S]](init: String = "")(implicit tx: S#Tx): UGenSource[S] = ??? // Impl(init)

  implicit def serializer[S <: evt.Sys[S]]: serial.Serializer[S#Tx, S#Acc, UGenSource[S]] =
    ??? // Impl.serializer[S]
}
trait UGenSource[S <: evt.Sys[S]] extends Attribute[S] {
  type Peer = stm.Source[S#Tx, Option[_UGenSource[_]]] // Expr.Var[S, String]

  def spec: UGenSpec

  def mkCopy()(implicit tx: S#Tx): UGenSource[S]
}