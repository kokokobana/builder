package builder.containers

import builder.components.{NumberInput, svg}
import builder.raw.ReactResponsiveSelect
import builder.raw.onsen.{Button, SearchInput, Switch}
import builder.state.SearchParameters
import builder.{Context, MainCircuit, SetItemSearchParams, Theme}
import japgolly.scalajs.react._
import japgolly.scalajs.react.raw.SyntheticKeyboardEvent
import japgolly.scalajs.react.vdom.html_<^._
import org.bitbucket.wakfuthesaurus.shared.data.{ItemEffectFilter, ItemFilter, ItemRarity}
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.dom.{Element, window}
import scalacss.ScalaCssReact._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

object EquipmentSearchForm {
  final case class Props(ctx: Context, params: SearchParameters, onSubmit: SearchParameters => Callback)

  def apply(ctx: Context, onSubmit: Callback): VdomElement =
    MainCircuit.connect(_.equipmentSearch.params).apply { model =>
      component(Props(ctx, model.value, params => onSubmit *> model.dispatchCB(SetItemSearchParams(params))))
    }

  private val component = ScalaComponent
    .builder[Props]("EquipmentSearchForm")
    .initialStateFromProps(_.params)
    .renderBackend[Backend]
    .componentDidMount { $ => $.backend.installEnterHandler($.getDOMNode.asElement) }
    .build

  class Backend($: BackendScope[Props, SearchParameters]) {
    def handleSearchInput(value: String): Callback =
      $.modState(_.copy(name = if (value.isEmpty) None else Some(value)))

    def installEnterHandler(node: Element): Callback = Callback {
      window.setTimeout(() => {
        node.getElementsByClassName("search-input")
          .apply(0)
          .addEventListener("keydown", (ev: SyntheticKeyboardEvent[HTMLInputElement]) => {
            if (ev.which == 13) {
              $.props.runNow().onSubmit($.state.runNow()).runNow()
            }
          })
      }, 100)
    }

    def renderSelectOption(text: String): VdomElement =
      <.div(
        <.span(^.className := "checkbox", svg.checkboxIcon),
        <.span(text)
      )

    def renderSelect(ctx: Context,
                     options: js.Array[ReactResponsiveSelect.Option],
                     selectedValues: js.Array[String],
                     onChange: ReactResponsiveSelect.MultiChange => Callback): VdomElement =
      <.div(Theme.multiselect,
        ReactResponsiveSelect[ReactResponsiveSelect.MultiChange](
          multiselect = true,
          options = options.toJSArray,
          onChange = js.defined(onChange),
          selectedValues = selectedValues.toJSArray,
          caretIcon = svg.caretIcon,
          customLabelRenderer = js.defined({ v =>
            if (!js.isUndefined(v.options)) {
              <.span(v.options.map(_.text).mkString(", ")).rawNode
            } else ctx.localization.ui("any").rawNode
          })
        )
      )

    def renderLevelInputs(ctx: Context,
                          min: Int,
                          max: Int,
                          onMinChange: Int => Callback,
                          onMaxChange: Int => Callback): VdomElement =
      <.div(Theme.itemsLevelRange,
        ctx.localization.ui("level_short"),
        NumberInput(Some(min), onMinChange, 1, 200),
        "-",
        NumberInput(Some(max), onMaxChange, 1, 200)
      )

    private[this] val itemFilterOptions = ItemFilter.values.map { i =>
      val name = $.props.runNow().ctx.translation.itemType(i.entryName)
      ReactResponsiveSelect.Option(name, i.entryName, renderSelectOption(name).rawNode)
    }.toJSArray

    private[this] val itemEffectFilterOptions = ItemEffectFilter.values.map { i =>
      val name = $.props.runNow().ctx.translation.characteristic(i.entryName)
      ReactResponsiveSelect.Option(name, i.entryName, renderSelectOption(name).rawNode)
    }.toJSArray

    private[this] val rarityOptions = {
      val props = $.props.runNow()
      ReactResponsiveSelect.Option(props.ctx.translation.ui("any"), "-1",
        renderSelectOption(props.ctx.translation.ui("any")).rawNode) +:
        ItemRarity.values.map { i =>
          val name = props.ctx.translation.itemRarity(i.entryName)
          ReactResponsiveSelect.Option(name, i.value.toString, renderSelectOption(name).rawNode)
        }
    }.toJSArray

    def render(props: Props, state: SearchParameters): VdomElement =
      <.section(^.style := js.Dynamic.literal("textAlign" -> "center"),
        <.p(
          SearchInput(
            value = js.defined(state.name.getOrElse("")),
            onChange = ev => handleSearchInput(ev.target.value),
            placeholder = props.ctx.translation.ui("item_name")
          )
        ),
        renderLevelInputs(
          props.ctx,
          state.minLevel,
          state.maxLevel,
          onMinChange = v => $.modState(_.copy(minLevel = v)),
          onMaxChange = v => $.modState(_.copy(maxLevel = v))
        ),
        renderSelect(
          ctx = props.ctx,
          options = itemFilterOptions,
          selectedValues = state.typeFilters.map(_.entryName).toJSArray,
          onChange = change => $.modState(_.copy(typeFilters = change.options.map(n => ItemFilter.withName(n.value))))
        ),
        renderSelect(
          ctx = props.ctx,
          options = itemEffectFilterOptions,
          selectedValues = state.effectFilters.map(_.entryName).toJSArray,
          onChange = change => $.modState(_.copy(effectFilters = change.options.map(n => ItemEffectFilter.withName(n.value))))
        ),
        renderSelect(
          ctx = props.ctx,
          options = rarityOptions,
          selectedValues = {
            val rarities = state.rarities.map(_.value.toString)
            if (rarities.isEmpty) Seq("-1") else rarities
          }.toJSArray,
          onChange = { change =>
            val out = change.options.toSeq.map(_.value) match {
              case Seq("-1") => Seq.empty[ItemRarity]
              case values =>
                values.map(n => ItemRarity.withValue(n.toInt))
            }
            $.modState(_.copy(rarities = out))
          }
        ),
        <.p(Theme.switchRow,
          Switch(
            checked = state.onlyBestCraft,
            onChange = ev => $.modState(_.copy(onlyBestCraft = ev.target.checked))
          ),
          <.span(Theme.label, props.ctx.localization.ui("only_best_craft"))
        ),
        <.p(Button(onClick = () => props.onSubmit(state))(props.ctx.localization.ui("search")))
      )
  }
}
