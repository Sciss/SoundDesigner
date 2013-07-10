package de.sciss.connect

import de.sciss.desktop.impl.SwingApplicationImpl
import de.sciss.desktop.Menu
import de.sciss.synth.proc.InMemory
import de.sciss.connect.view.Pane

object Application extends SwingApplicationImpl("Connect") {
  protected lazy val menuFactory = Menu.Root()

  type Document = Unit

  override protected def init() {
    type S = InMemory
    val  S = InMemory

    implicit val system = S()
    val pane = system.step { implicit tx =>
      val patcher = Patcher[S]
      Pane(patcher)
    }
    // println(s"pane $pane, component ${pane.component}")
    new view.Window(pane)
  }
}