package de.sciss.connect

import de.sciss.lucre.{event => evt, expr, stm}
import evt.EventLike
import expr.Expr
import de.sciss.synth.{MaybeRate, UGenSpec, proc}
import proc.Sys
import de.sciss.serial.Writable
import stm.Disposable
import collection.immutable.{IndexedSeq => Vec}
import reflect.runtime.universe.TypeTag
import language.existentials

/* sealed */ trait Element[S <: Sys[S]] extends Disposable[S#Tx]

object Product {
  sealed trait Update[S <: Sys[S]] { def elem: Product[S] }
  case class SpecChanged[S <: Sys[S]](elem: Product[S], change: evt.Change[UGenSpec]) extends Update[S]

  object Modifiable {
    trait Args[-Tx] extends Product.Args[Tx] {
      def update(idx: Int, value: Any)(implicit tx: Tx): Unit
    }
  }
  trait Modifiable[S <: Sys[S]] extends Product[S] {
    // override def spec: Expr.Var[S, UGenSpec ]
    // override def rate: Expr.Var[S, MaybeRate]

    override def arg: Modifiable.Args[S#Tx]
  }

  //  trait ArgLike {
  //    type T
  //    type Spec
  //    def name: String
  //    def tpe : TypeTag[T]
  //    def default: Option[T]
  //    def spec: Option[Spec]
  //  }
  // case class Arg[T, Spec](name: String, tpe: TypeTag[T], default: Option[T], spec: Option[Spec]) extends ArgLike

  case class ArgSpec(name: String, tpe: TypeTag[_], default: Option[Any], spec: Option[Any])

  //  trait ArgSource[-Tx] {
  //    def apply(idx: Int)(implicit tx: Tx): Option[Any]
  //  }
  //
  //  trait ArgSink[-Tx] {
  //    def apply(idx: Int)(implicit tx: Tx): Option[Any]
  //  }

  trait Args[-Tx] {
    def apply(idx: Int)(implicit tx: Tx): Option[Any]
  }
}
trait Product[S <: Sys[S]] extends /* Writable with */ Element[S] /* Disposable[S#Tx] */ {
  def prefix: String
  def numArgs: Int
  def argSpec(idx: Int): Product.ArgSpec
  def arg: Product.Args[S#Tx]

  //  def argName   (idx: Int): String
  //  def argDefault(idx: Int): Option[Any]
  //  def argSpec   (idx: Int): Option[Any]

  def value(implicit tx: S#Tx): Any

  def changed: EventLike[S, Product.Update[S], Product[S]]
}

object Element {
  import scala.{Int => _Int}

  object Int {
    trait Var[S <: Sys[S]] extends Int[S] {
      def value_=(i: _Int)(implicit tx: S#Tx): Unit
    }
  }
  trait Int[S <: Sys[S]] extends Element[S] {
    def value(implicit tx: S#Tx): _Int
  }
}