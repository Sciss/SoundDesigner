package de.sciss.connect
package sound

import de.sciss.lucre.synth.Sys
import de.sciss.synth.proc.AuralSystem
import de.sciss.connect.sound.impl.{AuralPresentationImpl => Impl}
import de.sciss.lucre.stm
import de.sciss.lucre.stm.Disposable

object AuralPresentation {
  def apply[S <: Sys[S]](aural: AuralSystem, patch: Patcher[S])
                        (implicit tx: S#Tx, cursor: stm.Cursor[S]): AuralPresentation[S] = Impl(aural, patch)
}
trait AuralPresentation[S <: Sys[S]] extends Disposable[S#Tx]
