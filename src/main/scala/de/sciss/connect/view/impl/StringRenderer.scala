package de.sciss.connect
package view
package impl

import java.awt.{Color, Shape, Graphics2D}
import java.awt.geom.{Line2D, Rectangle2D}
import de.sciss.synth.UGenSpec

trait StringRendererLike extends ElementRenderer {
  private val r = new Rectangle2D.Float()

  protected def dataToString(data: VisualBoxLike): String

  def getShape(x: Double, y: Double, data: VisualBoxLike): Shape = {
    val fm    = BoxRenderer.defaultFontMetrics
    val w1    = fm.stringWidth(dataToString(data))
    val w2    = math.max(BoxRenderer.MinBoxWidth, w1 + 6)
    val ports = data.ports
    val w3    = math.max(ports.numIns, ports.numOuts) * VisualPorts.MinSpacing
    val w     = math.max(w2, w3)
    r.setRect(x, y, w, BoxRenderer.DefaultBoxHeight)
    r
  }

  def paint(g: Graphics2D, bounds: Rectangle2D, data: VisualBoxLike): Unit = {
    val x   = bounds.getX.toFloat
    val y   = bounds.getY.toFloat
    // g.setFont(Style.font)
    val fm  = g.getFontMetrics
    g.drawString(dataToString(data), x + 3, y + 2 + fm.getAscent)
  }
}

object ToStringRenderer extends StringRendererLike {
  protected def dataToString(data: VisualBoxLike) = data.value.toString
}

object BooleanRenderer extends ElementRenderer {
  final val DefaultWidth  = 16
  final val DefaultHeight = 16

  private val ln  = new Line2D.Float()
  private val r   = new Rectangle2D.Float()

  def getShape(x: Double, y: Double, data: VisualBoxLike): Shape = {
    r.setRect(x, y, DefaultWidth, DefaultHeight)
    r
  }

  def paint(g: Graphics2D, bounds: Rectangle2D, data: VisualBoxLike): Unit =
    data.value match {
      case true =>
        g.setColor(Color.black)
        ln.setLine(bounds.getMinX + 2, bounds.getMinY + 2, bounds.getMaxX - 2, bounds.getMaxY - 2)
        g.draw(ln)
        ln.setLine(bounds.getMinX + 2, bounds.getMaxY - 2, bounds.getMaxX - 2, bounds.getMinY + 2)
        g.draw(ln)
      case _ =>
    }
}
object UGenSourceRenderer extends StringRendererLike {
  protected def dataToString(data: VisualBoxLike) = data.value match {
    case spec: UGenSpec => spec.name
    case _ => "???"
  }
}