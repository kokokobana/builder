package builder.containers

import builder._
import builder.components.NumberInput
import builder.raw.onsen._
import builder.state._
import builder.util.EffectList
import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.StateSnapshot
import japgolly.scalajs.react.raw.SyntheticMouseEvent
import japgolly.scalajs.react.vdom.html_<^._
import monocle.Lens
import org.bitbucket.wakfuthesaurus.shared.data._
import org.scalajs.dom.raw.HTMLInputElement
import scalacss.ScalaCssReact._

import scala.Function.const

object RuneDialog {
  final case class Props(
    context: Context,
    state: Option[StateSnapshot[Option[Rune]]],
    slotType: Option[RuneSlotType],
    onClose: Callback
  )

  def apply(
    context: Context,
    state: Option[StateSnapshot[Option[Rune]]],
    slotType: Option[RuneSlotType],
    onClose: Callback
  ): VdomElement = component(Props(context, state, slotType, onClose))

  private val component = ScalaFnComponent[Props] { case Props(ctx, maybeSlot, slotType, onClose) =>
    maybeSlot match {
      case Some(slot) =>
        val types = slotType.collect {
          case RuneSlotType.Supportive => RuneType.supportive
          case RuneSlotType.Offensive => RuneType.offensive
          case RuneSlotType.Defensive => RuneType.defensive
        }.getOrElse(Seq.empty)

        val value = slot.value.fold("-1")(_.`type`.entryName)
        AlertDialog(isOpen = true, isCancelable = false)(
          <.div(^.className := "alert-dialog-title")(ctx.localization.ui("customize_rune")),
          <.div(Theme.runePopupContent, ^.className := "alert-dialog-content")(
            Select(
              onChange = { ev =>
                RuneType.withNameOption(ev.target.value) match {
                  case Some(rt) => slot.modState(st => Some(st.fold(Rune(rt, 10))(_.copy(`type` = rt))))
                  case None => slot.setState(None)
                }
              },
              value = value
            )(
              <.option(^.value := "-1")(ctx.localization.ui("empty_rune")),
              types.map { tp =>
                <.option(^.key := tp.entryName, ^.value := tp.entryName, ctx.localization.itemRune(tp.entryName))
              }.toVdomArray
            ),
            slot.value match {
              case Some(rune) =>
                <.div(Theme.runeLevelInputContainer,
                  <.div(Theme.runeLevelInput,
                    NumberInput(
                      initialValue = Some(rune.level),
                      onChange = i => slot.modState(_.map(_.copy(level = i))),
                      min = 0,
                      max = 10
                    )
                  ), ctx.localization.ui("level_short")
                )
              case None => EmptyVdom
            }
          ),
          <.div(^.className := "alert-dialog-footer")(
            <.button(
              ^.className := "alert-dialog-button",
              ^.onClick --> onClose
            )("OK")
          )
        )
      case None =>
        AlertDialog(isOpen = false, isCancelable = false)(
          <.div(^.className := "alert-dialog-title")("Customize a rune slot"),
          <.div(^.className := "alert-dialog-content"),
          <.div(^.className := "alert-dialog-footer")
        )
    }
  }
}

object ItemPage {
  final case class Props(
    context: Context,
    state: StateSnapshot[SlotState],
    onBack: Callback
  )

  final case class State(dialog: Option[Int])

  def apply(ctx: Context, lens: Lens[Equipment[SlotState], Option[SlotState]], onBack: Callback) =
    MainCircuit.connect(state => state.character.equipment).apply { slot =>
      val snapshot =
        StateSnapshot(slot.value) {
          (v, _) => CallbackOption.liftOption(v).flatMap(s => slot.dispatchCB(UpdateEquipment(const(s))))
        }.zoomStateL(lens).xmapState(_.get)(Some(_))
      component(Props(ctx, snapshot, onBack))
    }

  private val component = ScalaComponent.builder[Props]("ItemPage")
    .initialState(State(None))
    .renderBackend[Backend]
    .build

  class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State): VdomElement = {
      val Props(ctx, slot, onBack) = props
      val runeSnapshot = state.dialog.map { i =>
        slot.zoomStateL(SlotState.runes).zoomState(_(i))(v => _.updated(i, v))
      }
      Page(renderToolbar = renderToolbar(ctx, slot.value.item, onBack))(
        <.div(Theme.itemHeaderRow,
          ctx.localization.ui("level_short"),
          " ",
          slot.value.level,
          " ",
          ctx.localization.itemRarity(ItemRarity.withValue(slot.value.item.rarity).entryName),
          " ",
          slot.value.item.typeName
        ).render,
        renderEffects(ctx, slot),
        if (slot.value.runes.nonEmpty)
          <.div(Theme.itemHeaderRow, ctx.localization.ui("runes")).render
        else EmptyVdom,
        renderRunes(ctx, slot),
        RuneDialog(ctx, runeSnapshot, slot.value.getRuneSlotType, $.setState(State(None)))
      )
    }

    val itemElements: Seq[Element] = Seq(Element.Fire, Element.Water, Element.Earth, Element.Air)

    def renderRunes(ctx: Context, slot: StateSnapshot[SlotState]): VdomElement = {
      val style = slot.value.getRuneSlotType.collect {
        case RuneSlotType.Defensive => Theme.defensiveRuneSlot
        case RuneSlotType.Offensive => Theme.offensiveRuneSlot
        case RuneSlotType.Supportive => Theme.supportRuneSlot
      }.getOrElse(Theme.itemRuneSlot)
      <.div(Theme.itemRunes,
        slot.value.runes.zipWithIndex.map { case (maybeRune, i) =>
          <.div(Theme.itemRuneRow,
            ^.key := i,
            ^.onClick --> $.setState(State(Some(i))),
            <.div(style, maybeRune.map {
              rune => ctx.assets.icon(s"runes/${rune.`type`.entryName}.png", Some(Theme.itemRuneIcon))
            }),
            <.span(Theme.itemRuneLabel,
              maybeRune.fold(ctx.translation.ui("empty_rune")) { rune =>
                util.item.formatRune(rune.`type`, ctx.translation, rune.level)
              }
            )
          )
        }.toVdomArray
      )
    }

    def renderToolbar(ctx: Context, item: Item, onBack: Callback): () => VdomElement = () =>
      Toolbar(modifier = "transparent")(
        <.div(^.className := "left")(BackButton(onClick = () => onBack)),
        <.div(^.className := "center",
          Theme.toolbarHeader,
          ctx.assets.itemIcon(item.gfxId, Some(Theme.toolbarHeaderIcon)),
          item.name
        )
      )

    def renderElementBoxes(ctx: Context, state: StateSnapshot[List[Element]], count: Int): VdomElement = {
      def onCheckboxChange(elem: Element)(checked: Boolean): Callback = {
        state.modState { elems =>
          if (checked) elem :: elems.filterNot(_ == elem)
          else elems
        }
      }

      <.div(Theme.itemElementsSelect,
        itemElements.map { elem =>
          val checked = state.value.take(count).contains(elem)
          <.div(Theme.itemElementsBox,
            ^.key := elem.value,
            ^.onClick --> onCheckboxChange(elem)(!checked),
            <.div(^.className := "checkbox-container")(
              <.input(^.`type` := "checkbox",
                ^.checked := checked,
                ^.onChange ==> { ev =>
                  onCheckboxChange(elem)(ev.asInstanceOf[SyntheticMouseEvent[HTMLInputElement]].target.checked)
                }
              ),
              <.label(<.div),
              ctx.assets.icon(s"elements/small_${elem.entryName}.png", Some(Theme.itemElementsIcon))
            )
          )
        }.toVdomArray
      )
    }

    def renderEffects(ctx: Context, slot: StateSnapshot[SlotState]): VdomElement =
      <.div(Theme.expandedItemEffects,
        slot.value.item.equipEffects.zipWithIndex.flatMap { case (eff, i) =>
          ActionType.byId(eff.action).collect {
            case ActionType.VariableElementsMasteryGain =>
              val elemCount = eff.originalParams(2).toInt
              <.div(
                ^.key := i,
                EffectList.renderEntry(ctx)(eff, slot.value.level),
                renderElementBoxes(ctx, slot.zoomStateL(SlotState.variableElementMastery), elemCount)
              ).render
            case ActionType.VariableElementsResGain =>
              val elemCount = eff.originalParams(2).toInt
              <.div(
                ^.key := i,
                EffectList.renderEntry(ctx)(eff, slot.value.level),
                renderElementBoxes(ctx, slot.zoomStateL(SlotState.variableElementResist), elemCount)
              ).render
            case _ => EffectList.renderEntry(ctx)(eff, slot.value.level, Some(i))
          }
        }.toVdomArray
      )
  }
}
