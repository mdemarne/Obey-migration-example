import scala.meta.internal.{ ast => impl }
import scala.meta._
import scala.meta.dialects.Scala211
import scala.meta.tql._
import scala.obey.model._
import scala.language.reflectiveCalls

@Tag("AkkaMigration") object AkkaMigration extends FixRule {

  def description = "Moves deprecated Scala Actors into Akka Actors"

  def message(t: impl.Source): Message = Message(s"Migrating...", t)

  /* Changes act to receive */
  val transformDef = (focus {
    case impl.Defn.Class(_, _, _, _, impl.Template(_, parents, _, _)) =>
      parents.exists(p => p match {
        case pp: impl.Name => pp.value == "Actor"
        case _ => false
      })
  } andThen (transform {
    case t @ impl.Defn.Def(_, nm, _, _, _, body1) if nm.value == "act" =>
      val findPartialFunction = (collect {
        case impl.Term.Apply(impl.Term.Name("receive"), (body2: impl.Term.PartialFunction) :: Nil) =>
          body2
      }).topDownBreak
      val ret = findPartialFunction(body1).result.headOption match {
        case Some(newBody) =>
          t.copy(name = impl.Term.Name("receive"), body = newBody.asInstanceOf[impl.Term], decltpe = None) andCollect Message("Moving actor PartialFunction...", t)
        case None => t andCollect Message("Did not find a receive partial function!", t)
      }
      ret
  }).topDownBreak).topDown

  /* Change import clause */
  val changeImport = (transform {
    case s: impl.Import if s.show[Code].contains("scala.actors") =>
      q"import akka.actor._".asInstanceOf[impl.Import] andCollect Message("Changing import clause.", s)
  }).topDown

  def apply = transformDef + changeImport
}
