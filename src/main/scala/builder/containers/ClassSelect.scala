package builder.containers

import builder.state.ClassState
import builder.{Context, MainCircuit, SetClass}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import org.bitbucket.wakfuthesaurus.shared.data.CharacterClass
import org.scalajs.dom.raw.HTMLSelectElement

object ClassSelect {
  final case class Props(context: Context, model: ModelProxy[ClassState])

  def apply(context: Context): VdomElement =
    MainCircuit.connect(_.character.`class`).apply { model =>
      component(Props(context, model))
    }

  private def options(ctx: Context) =
    CharacterClass.values.map { c =>
      <.option(^.key := c.value, ^.value := c.value.toString)(ctx.localization.characterClass(c.entryName))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
      <.select(
        ^.value := model.value.`class`.value.toString,
        ^.onChange ==> { ev =>
          model.dispatchCB(SetClass(CharacterClass.withValue(ev.target.asInstanceOf[HTMLSelectElement].value.toInt)))
        }
      )(options(ctx).toVdomArray)
    }
}
