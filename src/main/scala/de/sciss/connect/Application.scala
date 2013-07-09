package de.sciss.connect

import de.sciss.desktop.impl.SwingApplicationImpl
import de.sciss.desktop.Menu
import de.sciss.synth.proc.InMemory

object Application extends SwingApplicationImpl("Connect") {
  protected val menuFactory = Menu.Root()

  type Document = Unit

  override protected def init() {
    implicit val system = InMemory()
    new view.Window[InMemory]
  }
}