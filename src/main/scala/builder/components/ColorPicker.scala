package builder.components

import builder.Theme
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount
import japgolly.scalajs.react.extra.StateSnapshot
import japgolly.scalajs.react.raw.SyntheticMouseEvent
import japgolly.scalajs.react.vdom.html_<^._
import org.bitbucket.wakfuthesaurus.shared.data.Color
import org.scalajs.dom.raw.HTMLElement
import scalacss.ScalaCssReact._

import scala.scalajs.js

object ColorPicker {
  final case class Props(
    selected: StateSnapshot[Color],
    options: Seq[Color]
  )

  final case class State(open: Boolean)

  def apply(
    value: StateSnapshot[Color],
    options: Seq[Color]
  ): VdomElement = component(Props(value, options))

  def rgbaFromColor(color: Color): String =
    f"rgba(${color.red*255}%3.0f, ${color.green*255}%3.0f, ${color.blue*255}%3.0f, ${color.alpha*255}%3.0f)"

  def styleFromColor(color: Color, property: String = "backgroundColor"): js.Object = {
    js.Dynamic.literal(property -> rgbaFromColor(color))
  }

  def onMount($: ComponentDidMount[Props, State, Unit]): Callback = Callback {
    import org.scalajs.dom.window

    @scala.annotation.tailrec
    def isInColorDropdownContainer(node: HTMLElement): Boolean =
      if (node.id == "color-dropdown") true
      else {
        if (node.parentElement == null) false
        else isInColorDropdownContainer(node.parentElement)
      }

    window.document.addEventListener("click", { ev: SyntheticMouseEvent[HTMLElement] =>
      if (!isInColorDropdownContainer(ev.target)) {
        $.setState(State(false)).runNow()
      }
    }, true)
  }

  private val component = ScalaComponent.builder[Props]("ColorPicker")
    .initialState(State(false))
    .render { $ =>
      <.div(
        <.div(Theme.colorSelect,
          ^.onClick --> $.setState(State(true)),
          <.div(Theme.colorValue, ^.style := styleFromColor($.props.selected.value))
        ),
        if ($.state.open) {
          <.div(Theme.colorDropdownContainer,
            ^.id := "color-dropdown",
            <.div(Theme.colorDropdownTriangle,
              ^.style := styleFromColor($.props.selected.value, "borderBottomColor")
            ),
            <.div(Theme.colorDropdown,
              <.div(Theme.colorDropdownHeader,
                rgbaFromColor($.props.selected.value),
                ^.style := styleFromColor($.props.selected.value)
              ),
              <.div(Theme.colorOptions,
                $.props.options.map { color =>
                  <.div(Theme.colorOption,
                    ^.onClick --> $.props.selected.setState(color),
                    ^.style := styleFromColor(color))
                }.toVdomArray
              )
            )
          )
        } else EmptyVdom
      )
    }
    .componentDidMount(onMount)
    .build
}
