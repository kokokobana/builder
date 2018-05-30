package builder.containers

import builder._
import builder.containers.EquipmentView.Route
import builder.raw.onsen.Navigator.NavHandler
import builder.raw.onsen._
import builder.state.{Equipment, EquipmentToastState, RingSelectState, SlotState}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import monocle.Lens

import scala.scalajs.js

object RingSelect {
  final case class Props(
    context: Context,
    model: ModelProxy[RingSelectState]
  )

  def apply(context: Context) =
    MainCircuit.connect(_.ringSelect).apply { model =>
      component(Props(context, model))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    def equipButton(lens: Lens[Equipment[SlotState], Option[SlotState]], label: VdomNode): VdomElement =
      <.button(
        ^.className := "alert-dialog-button",
        ^.onClick --> CallbackOption.liftOption(model.value.alertItem).flatMap { slot =>
          model.dispatchCB(SetRingSelect(None)) *>
            model.dispatchCB(UpdateEquipment(lens.set(Some(slot))))
        }
      )(label)

    AlertDialog(isOpen = model.value.alertItem.isDefined, isCancelable = false)(
      <.div(^.className := "alert-dialog-title")("Please select a ring slot"),
      <.div(^.className := "alert-dialog-content"),
      <.div(^.className := "alert-dialog-footer")(
        equipButton(Equipment.rightRing, ctx.localization.ui("equip_right")),
        equipButton(Equipment.leftRing, ctx.localization.ui("equip_left"))
      )
    )
  }
}

object EquipmentToast {
  final case class Props(
    context: Context,
    model: ModelProxy[EquipmentToastState]
  )

  def apply(context: Context) =
    MainCircuit.connect(_.equipmentToast).apply { model =>
      component(Props(context, model))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    Toast(isOpen = model.value.toastText.isDefined)(
      <.div(^.className := "message")(model.value.toastText.fold("")(identity))
    )
  }
}

object EquipmentBrowserPage {
  final case class Props(
    context: Context,
    nav: NavHandler[Route],
    model: ModelProxy[_]
  )

  def apply(context: Context, nav: NavHandler[Route]): VdomElement =
    MainCircuit.wrap(_.character) { model =>
      component(Props(context, nav, model))
    }

  private val component = ScalaComponent.builder[Props]("EquipmentBrowser")
    .renderBackend[Backend]
    .componentDidMount(_.props.model.dispatchCB(InitializeItemSearch))
    .build

  class Backend($: BackendScope[Props, Unit]) {
    def renderToolbar(ctx: Context, route: Route, nav: NavHandler[Route]): () => VdomElement = () =>
      Toolbar(modifier = "transparent")(
        <.div(^.className := "center", ctx.localization.ui(route.translationKey)),
        <.div(^.className := "right")(
          ToolbarButton(onClick = () => Callback(nav.pushPage(Route.Search)))(
            Icon(icon = "ion-search, material:md-search")
          )
        )
      )

    def render(props: Props): VdomElement =
      Page(
        onInfiniteScroll = js.defined(cb => props.model.dispatchCB(FetchItems) *> Callback.lift(cb) runNow()),
        renderToolbar = renderToolbar(props.context, Route.Browse, props.nav),
        renderBottomToolbar = () => EquipmentToolbar(props.context, props.nav)
      )(
        ItemList(props.context, slot => props.model.dispatchCB(ItemClicked(slot))),
        RingSelect(props.context),
        if (util.common.isTouchDevice) EquipmentToast(props.context) else EmptyVdom
      )
  }
}
