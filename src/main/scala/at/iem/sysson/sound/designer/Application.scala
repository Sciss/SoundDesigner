package at.iem.sysson.sound.designer

import scala.swing.SwingApplication

object Application extends SwingApplication {
  def startup(args: Array[String]): Unit = DesignerView()
}