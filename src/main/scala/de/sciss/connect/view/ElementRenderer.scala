package de.sciss.connect
package view

import java.awt.{Graphics2D, Shape}
import java.awt.geom.Rectangle2D

trait ElementRenderer {
  def getShape(x: Double, y: Double           , data: VisualElement): Shape

  def paint(g: Graphics2D, bounds: Rectangle2D, data: VisualElement): Unit
}