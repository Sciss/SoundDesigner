package de.sciss.connect
package view

import de.sciss.synth.proc.Attribute
import scala.swing.Component
import de.sciss.lucre.stm
import impl.{PaneImpl => Impl}
import language.implicitConversions
import de.sciss.lucre.synth.Sys

object Pane {
  type Factory[S <: Sys[S]] = S#Tx => String => Option[Attribute[S]]

  trait ConfigLike[S <: Sys[S]] {
    def factory: Factory[S]
  }
  object Config {
    def apply[S <: Sys[S]]() = new ConfigBuilder[S]
    implicit def build[S <: Sys[S]](b: ConfigBuilder[S]): Config[S] = b.build
  }
  final case class Config[S <: Sys[S]](factory: Factory[S]) extends ConfigLike[S]

  final class ConfigBuilder[S <: Sys[S]] extends ConfigLike[S] {
    var factory: Factory[S] = { _ => _ => None }
    def build: Config[S] = Config(factory)
  }

  def apply[S <: Sys[S]](patcher: Patcher[S], config: Config[S] = Config[S]())
                        (implicit tx: S#Tx, cursor: stm.Cursor[S]): Pane[S] = Impl[S](patcher, config)
}
trait Pane[S <: Sys[S]] {
  def component: Component
}