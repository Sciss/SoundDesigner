package de.sciss.connect
package view
package impl

import prefuse.render.{Renderer, AbstractShapeRenderer}
import java.awt.geom.Rectangle2D
import prefuse.util.ColorLib
import java.awt._
import prefuse.visual.VisualItem
import de.sciss.synth.proc.Sys

private[impl] object BoxRenderer {
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
  private final val portColr    = ColorLib.getColor( 80,  80, 128)
}
private[impl] final class BoxRenderer[S <: Sys[S]](d: PaneImpl[S]) extends AbstractShapeRenderer {
  import BoxRenderer._

  private val r   = new Rectangle2D.Float()
  // private val r2  = new Rectangle2D.Float()

  protected def getRawShape(vi: VisualItem): Shape = {
    var x    = vi.getX
    if (x.isNaN || x.isInfinity) x = 0.0
    var y    = vi.getY
    if (y.isNaN || y.isInfinity) y = 0.0

    d.getData(vi).fold[Shape] {
      r.setRect(x, y, MinBoxWidth, DefaultBoxHeight)
      r
    } { data =>
      data.renderer.getShape(x, y, data)
    }
  }

  override def render(g: Graphics2D, vi: VisualItem): Unit = {
    val r = getShape(vi)
    val b = r.getBounds2D
    g.setColor(fillColr)
    g.fill(r)

    d.getData(vi).foreach { data =>
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
      g.draw(r)
      g.setColor(if (data.state == ElementState.Edit) textColrEdit else textColr)
      g.setFont(Style.font)
      // val fm  = Renderer.DEFAULT_GRAPHICS.getFontMetrics(Style.font)

      data.renderer.paint(g, b, data)

    }

    // val x   = b.getX.toFloat
    // val y   = b.getY.toFloat
    // g.drawString(data.name, x + 3, y + 2 + fm.getAscent)

    //      data match {
    //        case vge: VisualGraphElem =>
    //          vge.content.foreach { ge =>
    //            d.getPorts(vi).foreach { ports =>
    //              val atOrig = g.getTransform
    //              g.translate(x, y)
    //              g.setColor(portColr)
    //              ports.inlets .foreach(g.fill(_))
    //              ports.outlets.foreach(g.fill(_))
    //              ports.active.foreach { p =>
    //                val r = p.visualRect(ports)
    //                g.setColor(colrSel)
    //                r2.setRect(r.getX - 1, r.getY - 1, r.getWidth + 2, r.getHeight + 2)
    //                g.fill(r2)
    //              }
    //              g.setTransform(atOrig)
    //            }
    //          }
    //        case vc: VisualConstant =>
    //
    //      }
  }
}