package de.sciss.connect
package view

import de.sciss.desktop.impl.WindowImpl
import de.sciss.desktop
import de.sciss.desktop.WindowHandler
import de.sciss.lucre.synth.Sys

class Window[S <: Sys[S]](pane: Pane[S]) extends WindowImpl {
  def handler: WindowHandler = Application.windowHandler

  protected def style = desktop.Window.Regular

  closeOperation = desktop.Window.CloseExit

  contents = pane.component

  pack()
  // centerOnScreen()
  front()
}