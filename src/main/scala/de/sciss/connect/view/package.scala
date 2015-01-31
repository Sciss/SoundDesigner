package de.sciss.connect

import scala.collection.immutable.{IndexedSeq => Vec}
import java.awt.EventQueue
import scala.util.control.NonFatal
import de.sciss.lucre.stm.Txn
import scala.concurrent.stm.{Txn => ScalaTxn, TxnLocal}

// XXX TODO: factor out with Mellite
package object view {
  private val guiCode = TxnLocal(init = Vec.empty[() => Unit], afterCommit = handleGUI)
  //   private lazy val primaryMod   = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask

  private def handleGUI(seq: Vec[() => Unit]): Unit = {
    def exec(): Unit =
      seq.foreach { fun =>
        try {
          fun()
        } catch {
          case NonFatal(e) => e.printStackTrace()
        }
      }

    execInGUI(exec())
  }

  def requireEDT(): Unit =
    require(EventQueue.isDispatchThread, "Called outside event dispatch thread")

  def execInGUI(code: => Unit): Unit = {
    require(ScalaTxn.findCurrent.isEmpty, "Calling inside transaction")
    if (EventQueue.isDispatchThread)
      code
    else
      EventQueue.invokeLater(new Runnable {
        def run(): Unit = code
      })
  }

  def guiFromTx(body: => Unit)(implicit tx: Txn[_]): Unit = {
    //      STMTxn.afterCommit( _ => body )( tx.peer )
    guiCode.transform(_ :+ (() => body))(tx.peer)
  }
}