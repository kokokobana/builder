package builder.containers
import builder._
import builder.raw.onsen.{BottomToolbar, Icon}
import builder.raw.onsen.Navigator.NavHandler
import builder.state.{Equipment, EquipmentSlot, SlotState}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._

object EquipmentToolbar {
  final case class Props(
    context: Context,
    model: ModelProxy[Equipment[SlotState]],
    nav: NavHandler[EquipmentView.Route]
  )

  def apply(context: Context, navHandler: NavHandler[EquipmentView.Route]): VdomElement =
    MainCircuit.connect(_.character.equipment).apply { model =>
      component(Props(context, model, navHandler))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model, nav) =>
    BottomToolbar(modifier = "equipment-bar")(
      Seq(EquipmentSlot.Headgear, EquipmentSlot.Cape, EquipmentSlot.Amulet, EquipmentSlot.Epaulettes,
        EquipmentSlot.Breastplate, EquipmentSlot.Belt, EquipmentSlot.LeftRing, EquipmentSlot.RightRing,
        EquipmentSlot.Boots, EquipmentSlot.Pet, EquipmentSlot.Costume, EquipmentSlot.LeftHand,
        EquipmentSlot.RightHand, EquipmentSlot.Emblem, EquipmentSlot.Mount
      ).zipWithIndex.map { case (slot, i) =>
        SlotComponent(ctx, i, slot, slot.lens.get(model.value), model.dispatchCB(UpdateEquipment(slot.lens.set(None))), nav)
      }.toVdomArray
    )
  }

  object SlotComponent {
    final case class Props(context: Context,
                           slot: EquipmentSlot,
                           slotState: Option[SlotState],
                           onRemove: Callback,
                           nav: NavHandler[EquipmentView.Route])

    final case class State(hovered: Boolean)

    implicit val propsReusability: Reusability[Props] = Reusability.by(_.slotState.map(_.item.id))
    implicit val stateReusability: Reusability[State] = Reusability.by_==

    def apply(ctx: Context,
              key: Int,
              slot: EquipmentSlot,
              slotState: Option[SlotState],
              onRemove: Callback,
              nav: NavHandler[EquipmentView.Route]): VdomElement =
      component.withKey(key)(Props(ctx, slot, slotState, onRemove, nav))

    private val component = ScalaComponent.builder[Props]("SlotComponent")
      .initialState(State(false))
      .render { $ =>
        val Props(ctx, slot, slotState, onRemove, nav) = $.props
        slotState match {
          case None =>
            <.div(Theme.equipmentSlotContainer,
              ctx.assets.icon(s"slots/${slot.icon}.png", Some(Theme.equipmentSlotIcon))
            )
          case Some(st) =>
            <.div(Theme.equipmentSlotContainer,
              ^.onMouseEnter --> $.setState(State(true)),
              ^.onMouseLeave --> $.setState(State(false)),
              ^.onClick --> Callback(nav.pushPage(EquipmentView.Route.Item(slot.lens))),
              ctx.assets.icon("slots/blank.png", Some(Theme.equipmentSlotIcon)),
              ctx.assets.itemIcon(st.item.gfxId, Some(Theme.equipmentItemIcon)),
              <.div(Theme.equipmentItemRemove,
                ^.onClick ==> { ev => ev.stopPropagationCB *> onRemove },
                Icon(icon = "times", size = 16, color = "red")
              ).when($.state.hovered)
            )
        }
      }.configure(Reusability.shouldComponentUpdate)
      .build
  }
}
