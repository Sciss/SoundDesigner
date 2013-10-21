package de.sciss.connect
package view
package impl

import prefuse.render.{AbstractShapeRenderer, EdgeRenderer}
import de.sciss.lucre.synth.Sys
import java.awt.{RenderingHints, BasicStroke, Shape, Graphics2D}
import prefuse.visual.{EdgeItem, VisualItem}
import java.awt.geom.{Line2D, AffineTransform, Point2D}

object CableRenderer {
  private final val portColr  = Style.portColor
  private final val strk      = new BasicStroke(1.5f)
}
final class CableRenderer[S <: Sys[S]](d: PaneImpl[S]) extends AbstractShapeRenderer {
  import CableRenderer._

  private val line = new Line2D.Double()

  protected def getRawShape(vi: VisualItem): Shape = {
    val edge      = vi.asInstanceOf[EdgeItem]
    val sourceVi  = edge.getSourceItem
    val sinkVi    = edge.getTargetItem

    for {
      edgeData   <- d.getEdgeData(edge    )
      sourceData <- d.getNodeData(sourceVi)
      sinkData   <- d.getNodeData(sinkVi  )
    } {
      val sourcePorts = sourceData.ports
      val sourceR     = sourcePorts.outlets(edgeData.outlet)
      val sinkPorts   = sinkData  .ports
      val sinkR       = sinkPorts  .inlets (edgeData.inlet )
      val x1          = sourceR.getCenterX + sourceVi.getX
      val y1          = sourceR.getCenterY + sourceVi.getY
      val x2          = sinkR  .getCenterX + sinkVi  .getX
      val y2          = sinkR  .getCenterY + sinkVi  .getY

      line.setLine(x1, y1, x2, y2)
    }

    strk.createStrokedShape(line)
  }

  override def render(g: Graphics2D, vi: VisualItem): Unit = {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setColor(portColr)
    val shp = getShape(vi)
    g.fill(shp)
  }
}
