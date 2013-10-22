package de.sciss.connect
package sound
package impl

import de.sciss.synth.proc.{Attribute, AuralSystem}
import de.sciss.lucre.synth.{Synth, Server, Sys}
import de.sciss.lucre.stm
import de.sciss.synth.UGenSpec
import scala.concurrent.stm.Ref

object AuralPresentationImpl {
  def apply[S <: Sys[S]](aural: AuralSystem, patch: Patcher[S])
                        (implicit tx: S#Tx, cursor: stm.Cursor[S]): AuralPresentation[S] = {
    val res = new Impl[S](aural, tx.newHandle(patch))
    aural.addClient(res)
    aural.serverOption.foreach(res.startedTx)
    res
  }

  private sealed trait AuralElem
  private final case class AuralUGenPorts(args: Map[String, AuralElem])
  private final class AuralUGen(val spec: UGenSpec) extends AuralElem {
    val ports   = Ref(AuralUGenPorts(Map.empty))
    val running = Ref(Option.empty[Synth])
  }

  private final class Impl[S <: Sys[S]](aural: AuralSystem, patchH: stm.Source[S#Tx, Patcher[S]])
                                       (implicit cursor: stm.Cursor[S])
    extends AuralPresentation[S] with AuralSystem.Client {

    def stopped() = ()

    def started(s: Server): Unit = cursor.step { implicit tx => startedTx(s) }

    def startedTx(s: Server)(implicit tx: S#Tx): Unit = {
      val patch   = patchH()
      val viewMap = tx.newInMemoryIDMap[AuralElem]
      patch.nodeIterator.foreach(addElem)
      patch.changed.react { implicit tx => { case Patcher.Update(p, changes) =>
        changes.foreach {
          case Patcher.NodeAdded  (elem) => addElem   (elem)
          case Patcher.NodeRemoved(elem) => removeElem(elem)
          case Patcher.NodeChanged(elem, upd) =>
            println(s"Not yet handled: AuralPresentation <- NodeChanged($elem, $upd)")
        }
      }}

      def addElem(elem: Attribute[S])(implicit tx: S#Tx): Unit = elem match {
        case a: UGenSource[S] =>
          val aural = new AuralUGen(a.spec)
          viewMap.put(a.id, aural)
          checkRun(aural)

        case a: Attribute.Int[S] =>
        case a: Attribute.Boolean[S] =>
        case a: Connection[S] =>
        case _ =>
      }

      def removeElem(elem: Attribute[S])(implicit tx: S#Tx): Unit = {
      }

      def checkRun(aural: AuralUGen)(implicit tx: S#Tx): Unit = {
        val ports = aural.ports()
        val spec  = aural.spec
        spec.inputs
        aural.spec.args.foreach { arg =>
          // name: String, tpe: UGenSpec.ArgumentType, defaults: Map[MaybeRate, UGenSpec.ArgumentValue],
          // rates: Map[MaybeRate, RateConstraint]
          aural.ports
        }
      }
    }

    def dispose()(implicit tx: S#Tx): Unit = {
      aural.removeClient(this)
    }
  }
}
