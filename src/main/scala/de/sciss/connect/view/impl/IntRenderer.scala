package de.sciss.connect
package view
package impl

import java.awt.{Shape, Graphics2D}
import java.awt.geom.Rectangle2D

object IntRenderer extends ElementRenderer {
  private val r = new Rectangle2D.Float()

  def getShape(x: Double, y: Double, data: VisualElement): Shape = {
    val w1 = data match {
      case i: VisualInt[_] =>
        val fm  = BoxRenderer.defaultFontMetrics
        fm.stringWidth(i.value.toString)
      case _ => 0
    }
    val w   = math.max(BoxRenderer.MinBoxWidth, w1)
    r.setRect(x, y, w, BoxRenderer.DefaultBoxHeight)
    r
  }

  def paint(g: Graphics2D, bounds: Rectangle2D, data: VisualElement) {
    data match {
      case i: VisualInt[_] =>
        val x   = bounds.getX.toFloat
        val y   = bounds.getY.toFloat
        // g.setFont(Style.font)
        val fm  = g.getFontMetrics
        g.drawString(i.value.toString, x + 3, y + 2 + fm.getAscent)
      case _ =>
    }
  }
}