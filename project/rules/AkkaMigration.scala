import scala.meta.{Term => _, Ctor => _,  Template => _, _}
import scala.meta.internal.ast._
import scala.meta.tql._
import scala.meta.dialects.Scala211
import scala.meta.semantic.Context
import scala.obey.model._
import scala.language.reflectiveCalls

@Tag("AkkaMigration") class AkkaMigration(implicit c: Context) extends FixRule {

  def description = "Moves deprecated Scala Actors into Akka Actors"

  def message(t: scala.meta.Tree): Message = Message(s"Migrating...", t)

  val transformDef = (transform {
      case cls @ Defn.Class(_, _, _, _, templ @ Template(_, parents, _, Some(stats)))
      if parents.exists(actor => actor.isInstanceOf[Ctor.Ref.Name] && actor.asInstanceOf[Ctor.Ref.Name].value == "Actor") =>
        val templ1 = templ.transform {
          case act: Defn.Def if act.name.toString == "act" =>
            val List(messages) = act.collect {
              case Term.Apply(receive: Term.Name, List(messages: Term.PartialFunction))
              if receive.value == "receive" =>
                messages
            }
            act.copy(name = q"receive", body = messages)
        }.asInstanceOf[Template]
        cls.copy(templ = templ1) andCollect Message("Moving actor PartialFunction...", templ)
    }).topDown

  /* Change import clause */
  val changeImport = (transform {
    case s: Import if s.show[Code].contains("scala.actors") =>
      q"import akka.actor._".asInstanceOf[Import] andCollect Message("Changing import clause.", s)
  }).topDown

  def apply = transformDef + changeImport
}
