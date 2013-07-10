//package de.sciss.connect
//
//import de.sciss.lucre.{event => evt, expr, stm}
//import evt.EventLike
//import de.sciss.synth.proc
//import de.sciss.synth.proc.{Attribute, Sys}
//import stm.Disposable
//import collection.immutable.{IndexedSeq => Vec}
//import reflect.runtime.universe.TypeTag
//import language.existentials
//// import impl.{ElementImpl => Impl}
//
/////* sealed */ trait NodeChanged[S <: Sys[S]] extends Disposable[S#Tx] {
////  def changed: EventLike[S, NodeChanged.Update[S], NodeChanged[S]]
////}
//
//object Product {
//
//  //  sealed trait Update[S <: Sys[S]] extends NodeChanged.Update[S] {
//  //    override def elem: Product[S]
//  //  }
//  // case class SpecChanged[S <: Sys[S]](elem: Product[S], change: evt.Change[UGenSpec]) extends Update[S]
//
//  object Modifiable {
//    trait Args[-Tx] extends Product.Args[Tx] {
//      def update(idx: Int, value: Any)(implicit tx: Tx): Unit
//    }
//  }
//  trait Modifiable[S <: Sys[S]] extends Product[S] {
//    override def arg: Modifiable.Args[S#Tx]
//  }
//
//  case class ArgSpec(name: String, tpe: TypeTag[_], default: Option[Any], spec: Option[Any])
//
//  trait Args[-Tx] {
//    def apply(idx: Int)(implicit tx: Tx): Option[Any]
//  }
//}
//trait Product[S <: Sys[S]] extends /* Writable with */ Attribute[S] /* Disposable[S#Tx] */ {
//  def prefix: String
//  def numArgs: Int
//  def argSpec(idx: Int): Product.ArgSpec
//  def arg: Product.Args[S#Tx]
//
//  def value(implicit tx: S#Tx): Any
//
//  // def changed: EventLike[S, Product.Update[S], Product[S]]
//}
//
////object NodeChanged {
////  import scala.{Int => _Int, Boolean => _Boolean}
////
////  sealed trait Update[S <: Sys[S]] { def elem: NodeChanged[S] }
////
////  object Int {
////    def newVar[S <: Sys[S]](init: _Int)(implicit tx: S#Tx): Var[S] = Impl.Int.newVar(init)
////
////    trait Var[S <: Sys[S]] extends Int[S] {
////      def value_=(i: _Int)(implicit tx: S#Tx): Unit
////    }
////  }
////  trait Int[S <: Sys[S]] extends NodeChanged[S] {
////    def value(implicit tx: S#Tx): _Int
////  }
////
////  object Boolean {
////    trait Var[S <: Sys[S]] extends Boolean[S] {
////      def value_=(i: _Boolean)(implicit tx: S#Tx): Unit
////    }
////  }
////  trait Boolean[S <: Sys[S]] extends NodeChanged[S] {
////    def value(implicit tx: S#Tx): _Boolean
////  }
////}