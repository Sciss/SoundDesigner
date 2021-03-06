package de.sciss.connect.view

import java.awt.geom.Rectangle2D
import collection.immutable.{IndexedSeq => Vec}

object VisualPorts {
  final val MinSpacing = 10

  def apply(numIns: Int, numOuts: Int): VisualPorts = new VisualPorts(numIns, numOuts)
}
final class VisualPorts private(val numIns: Int, val numOuts: Int) {
  val inlets  = Vec.fill(numIns )(new Rectangle2D.Float)
  val outlets = Vec.fill(numOuts)(new Rectangle2D.Float)
  var active  = Option.empty[Port]

  def isEmpty   = numIns == 0 && numOuts == 0
  def nonEmpty  = !isEmpty

  def update(bounds: Rectangle2D): Unit = {
    //      val x       = bounds.getX.toFloat
    //      val y       = bounds.getY.toFloat
    val x       = 0f
    val y       = 0f
    val w       = bounds.getWidth.toFloat
    val h       = bounds.getHeight.toFloat
    val wm      = w - 7
    if (numIns > 0) {
      val xf = if (numIns > 1) wm / (numIns - 1) else 0f
      var i = 0; while (i < numIns) {
        inlets(i).setRect(x + i * xf, y, 8, 3)
      i += 1 }
    }
    if (numOuts > 0) {
      val xf = if (numOuts > 1) wm / (numOuts - 1) else 0f
      val y2 = y + h - 2 /* 3 */
      var i = 0; while (i < numOuts) {
        outlets(i).setRect(x + i * xf, y2, 8, 3)
      i += 1 }
    }
  }
}
