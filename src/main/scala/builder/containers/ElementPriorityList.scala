package builder.containers
import builder.raw.onsen.Icon
import builder.{Context, MainCircuit, SetElementPriorities, Theme}
import builder.state.ElementPriorityState
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._
import cats.implicits._

object ElementPriorityList {
  final case class Props(context: Context, model: ModelProxy[ElementPriorityState])

  def apply(context: Context): VdomElement =
    MainCircuit.connect(_.equipmentSearch.elementPriorities).apply { model =>
      component(Props(context, model))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    val iconSize = if (builder.util.common.isTouchDevice) 30 else 16
    val priorityClass = if (builder.util.common.isTouchDevice) Theme.elementPriority else Theme.smallElementPriority
    <.ul(Theme.elementPriorityList,
      model.value.elements.zipWithIndex.map { case (elem, idx) =>
        <.li(priorityClass,
          ^.key := elem.value,
          ctx.assets.icon(s"elements/small_${elem.entryName}.png"),
          if (builder.util.common.isTouchDevice)
            <.span(Theme.elementPriorityLabel, ctx.localization.element(elem))
          else EmptyVdom,
          <.div(Theme.elementPriorityControls,
            <.div(
              ^.onClick --> Callback.lazily {
                val (higher, _ :: lower) = model.value.elements.splitAt(idx)
                val newOrder = higher.dropRight(1) ::: elem :: higher.takeRight(1) ::: lower
                model.dispatchCB(SetElementPriorities(newOrder))
              },
              Icon(
                icon = "chevron-up",
                size = iconSize, color = if (idx === 0) "grey" else "black"
              )
            ),
            <.div(
              ^.onClick --> Callback.lazily {
                val (higher, _ :: lower) = model.value.elements.splitAt(idx)
                val newOrder = higher ::: lower.take(1) ::: elem :: lower.drop(1)
                model.dispatchCB(SetElementPriorities(newOrder))
              },
              Icon(
                icon = "chevron-down",
                size = iconSize,
                color = if (idx === model.value.elements.length - 1) "grey" else "black"
              )
            )
          )
        )
      }.toVdomArray
    )
  }
}
