package de.sciss.connect
package view

import de.sciss.desktop.impl.WindowImpl
import de.sciss.desktop
import desktop.WindowHandler
import de.sciss.lucre.synth.Sys

class Window[S <: Sys[S]](pane: Pane[S]) extends WindowImpl {
  def handler: WindowHandler = Application.windowHandler

  protected def style = desktop.Window.Regular

  // private val pane = Pane[S](patcher)

  contents = pane.component

  pack()
  // centerOnScreen()
  front()
}