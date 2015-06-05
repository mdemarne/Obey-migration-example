import scala.meta.internal.{ast => impl}
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
        case _ => true
      })
  } andThen (transform {
    case t @ impl.Defn.Def(_, nm, _, _, _, body1) if nm.value == "act" =>
      val changeReceive = (transform {
        // Covering syntactic only case
        case impl.Term.Block(t @ impl.Term.While(impl.Lit.Bool(true), impl.Term.Block(List(impl.Term.Apply(impl.Term.Name("receive"), body2)))) :: Nil) =>
          body2.head
        // Covering semantic only case
        case t @ impl.Term.While(_, impl.Term.Apply(impl.Term.Name("receive"), body2)) =>
          body2.head
      }).topDown
      val newBody = changeReceive(body1)
      t.copy(name = impl.Term.Name("receive"), body = newBody.tree.get.asInstanceOf[impl.Term], decltpe = None) andCollect Message("Moving actor PartialFunction...", t)
  }).topDownBreak).topDown

  /* Change import clause */
  val changeImport = (transform {
    case s: impl.Import if s.show[Code].contains("scala.actors") =>
        q"import akka.actor.Actor".asInstanceOf[impl.Import] andCollect Message("Changing import clause.", s)
  }).topDown

  def apply = transformDef + changeImport
}
