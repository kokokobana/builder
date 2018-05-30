package builder.containers

import builder.components.ControlledNumberInput
import builder.state.CharacterLevelState
import builder.{Context, MainCircuit, SetLevel}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._

object LevelInput {
  final case class Props(context: Context, model: ModelProxy[CharacterLevelState])

  def apply(context: Context): VdomElement =
    MainCircuit.connect(_.character.level).apply { model =>
      component(Props(context, model))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    ControlledNumberInput(
      value = model.value.level,
      onChange = n => model.dispatchCB(SetLevel(n)),
      min = 1,
      max = 200
    )
  }
}
