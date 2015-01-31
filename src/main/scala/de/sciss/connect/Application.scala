package de.sciss.connect

import de.sciss.desktop.impl.SwingApplicationImpl
import de.sciss.desktop.Menu
import de.sciss.synth.proc._
import de.sciss.connect.view.Pane
import de.sciss.synth.UGenSpec
import de.sciss.lucre.synth.InMemory
import de.sciss.lucre.expr.{Boolean => BooleanEx, Int => IntEx}

object Application extends SwingApplicationImpl("Connect") {
  protected lazy val menuFactory = Menu.Root()

  type Document = Unit
  type S        = InMemory
  val  S        = InMemory

  override protected def init(): Unit = {
    implicit val system = S()
    val pane = system.step { implicit tx =>
      val patcher     = Patcher[S]
      val config      = Pane.Config[S]()
      config.factory  = { implicit tx: S#Tx => name => factory(name) }
      Pane(patcher, config)
    }
    new view.Window(pane)
  }

  private val Collection = UGenSpec.standardUGens

  def factory(name: String)(implicit tx: S#Tx): Option[Obj[S]] = {
    val imp = ExprImplicits[S]
    import imp._

    if (name.isEmpty) return None

    val name1 = name.trim
    val ch1   = name1.charAt(0)
    if ((ch1 == '-' || ch1.isDigit) && name1.substring(1).forall(_.isDigit)) {
      val i = name.toInt
      val v = IntEx.newVar[S](i)
      val a = Obj(IntElem(v))
      Some(a)

    } else if (name == "true" || name == "false") {
      val b = name.toBoolean
      val v = BooleanEx.newVar[S](b)
      val a = Obj(BooleanElem(v))
      Some(a)

    } else Collection.get(name1).map { uSpec =>
      Obj(UGenSource[S](uSpec))
    }
  }
}