package de.sciss.connect

import collection.immutable.{IndexedSeq => Vec}
import de.sciss.synth.proc.Elem
import de.sciss.lucre.{event => evt, data}

object Element {
  trait Port[S <: evt.Sys[S]] {
    def name: String
    def links(implicit tx: S#Tx): data.Iterator[S#Tx, Link[S]] // : Vec[Link[S]]
    def addLink   (link: Link[S])(implicit tx: S#Tx): Unit
    def removeLink(link: Link[S])(implicit tx: S#Tx): Boolean
  }

  trait Link[S <: evt.Sys[S]] {
    def targetElem: Elem[S]
    def targetPort     : Int
  }
}
trait Element[S <: evt.Sys[S]] {
  def inlets : Vec[Element.Port[S]]
  def outlets: Vec[Element.Port[S]]
}