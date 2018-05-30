package builder.components

import builder.raw.onsen.Input
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js

object NumberInput {
  final case class Props(
    initialValue: Option[Int],
    onChange: Int => Callback,
    min: Int,
    max: Int
  )
  final case class State(value: Option[Int])

  implicit val propsReusability: Reusability[Props] = Reusability.always
  implicit val stateReusability: Reusability[State] = Reusability.never

  def apply(
    initialValue: Option[Int],
    onChange: Int => Callback,
    min: Int,
    max: Int
  ): VdomElement = component(Props(initialValue, onChange, min, max))

  private val component = ScalaComponent.builder[Props]("NumberInput")
    .initialStateFromProps { props => State(props.initialValue) }
    .render { $ =>
        Input(
          `type` = "number",
          modifier = "underbar",
          value = $.state.value.fold("")(_.toString),
          onChange = js.defined { ev =>
            val value = ev.target.value
            val parsed =
              if (value.isEmpty) None
              else Some(math.min(math.max(value.toInt, $.props.min), $.props.max))
            $.setState(State(parsed), $.props.onChange(parsed.getOrElse($.props.min)))
          }
        )
    }
    .configure(Reusability.shouldComponentUpdate)
    .build
}
