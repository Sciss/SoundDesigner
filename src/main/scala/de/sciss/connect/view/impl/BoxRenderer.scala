package de.sciss.connect
package view
package impl

import prefuse.render.{Renderer, AbstractShapeRenderer}
import java.awt.geom.Rectangle2D
import prefuse.util.ColorLib
import java.awt._
import prefuse.visual.VisualItem
import de.sciss.lucre.synth.Sys

object BoxRenderer {
  final val MinBoxWidth         = 24
  final val DefaultBoxHeight    = 18

  def defaultFontMetrics: FontMetrics = Renderer.DEFAULT_GRAPHICS.getFontMetrics(Style.font)

  private final val colrSel     = Style.selectionColor
  private final val strkColrOk  = ColorLib.getColor(192, 192, 192)
  private final val strkColrEdit= colrSel
  private final val strkColrErr = ColorLib.getColor(240,   0,   0)
  private final val fillColr    = Style.boxColor
  private final val textColrEdit= strkColrEdit
  private final val textColr    = Color.black
  private final val strkShpOk   = new BasicStroke(1f)
  private final val strkShpPend = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, Array[Float](6, 4), 0f)
  private final val portColr    = Style.portColor
}
final class BoxRenderer[S <: Sys[S]](d: PaneImpl[S]) extends AbstractShapeRenderer {
  import BoxRenderer._

  private val r   = new Rectangle2D.Float()
  // private val r2  = new Rectangle2D.Float()

  protected def getRawShape(vi: VisualItem): Shape = {
    var x    = vi.getX
    if (x.isNaN || x.isInfinity) x = 0.0
    var y    = vi.getY
    if (y.isNaN || y.isInfinity) y = 0.0

    d.getNodeData(vi).fold[Shape] {
      r.setRect(x, y, MinBoxWidth, DefaultBoxHeight)
      r
    } { data =>
      data.renderer.getShape(x, y, data)
    }
  }

  override def render(g: Graphics2D, vi: VisualItem): Unit = {
    val shp = getShape(vi)
    val b   = shp.getBounds2D
    g.setColor(fillColr)
    g.fill(shp)

    d.getNodeData(vi).foreach { data =>
      data.state match {
        case ElementState.Ok =>
          g.setColor (strkColrOk)
          g.setStroke(strkShpOk )
        case ElementState.Edit =>
          g.setColor (strkColrEdit)
          g.setStroke(strkShpPend )
        case ElementState.Error =>
          g.setColor (strkColrErr)
          g.setStroke(strkShpPend)
      }
      g.draw(shp)
      g.setColor(if (data.state == ElementState.Edit) textColrEdit else textColr)
      g.setFont(Style.font)
      // val fm  = Renderer.DEFAULT_GRAPHICS.getFontMetrics(Style.font)

      data.renderer.paint(g, b, data)

      val ports   = data.ports
      if (ports.nonEmpty) {
        val atOrig  = g.getTransform
        val x       = b.getX.toFloat
        val y       = b.getY.toFloat
        g.translate(x, y)
        g.setColor(portColr)
        ports.inlets .foreach(g.fill)
        ports.outlets.foreach(g.fill)
        ports.active.foreach { p =>
          val r0 = p.visualRect(ports)
          g.setColor(colrSel)
          r.setRect(r0.getX - 1, r0.getY - 1, r0.getWidth + 2, r0.getHeight + 2)
          g.fill(r0)
        }
        g.setTransform(atOrig)
      }
    }
  }
}