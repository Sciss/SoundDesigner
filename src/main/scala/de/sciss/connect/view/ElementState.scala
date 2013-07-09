package de.sciss.connect
package view

object ElementState {
  case object Edit  extends ElementState
  case object Error extends ElementState
  case object Ok    extends ElementState
}
sealed trait ElementState