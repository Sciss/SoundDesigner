package de.sciss.connect
package view
package impl

import java.awt.{Shape, Graphics2D}
import java.awt.geom.Rectangle2D

object StringRenderer extends ElementRenderer {
  private val r = new Rectangle2D.Float()

  def getShape(x: Double, y: Double, data: VisualElement): Shape = {
    val fm  = BoxRenderer.defaultFontMetrics
    val w1  = fm.stringWidth(data.value.toString)
    val w   = math.max(BoxRenderer.MinBoxWidth, w1 + 6)
    r.setRect(x, y, w, BoxRenderer.DefaultBoxHeight)
    r
  }

  def paint(g: Graphics2D, bounds: Rectangle2D, data: VisualElement) {
    val x   = bounds.getX.toFloat
    val y   = bounds.getY.toFloat
    // g.setFont(Style.font)
    val fm  = g.getFontMetrics
    g.drawString(data.value.toString, x + 3, y + 2 + fm.getAscent)
  }
}