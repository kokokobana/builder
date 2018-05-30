package builder.containers
import builder._
import builder.raw.InfiniteScroll
import builder.raw.onsen._
import builder.state.{EquipmentSearchState, ItemWithCachedEffects, SlotState}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import org.bitbucket.wakfuthesaurus.shared.data.{Item, ItemRarity}
import scalacss.ScalaCssReact._

import scala.scalajs.js

object ItemList {
  final case class Props(
    context: Context,
    model: ModelProxy[EquipmentSearchState],
    onClick: SlotState => Callback
  )

  def apply(ctx: Context, onClick: SlotState => Callback): VdomElement =
    MainCircuit.connect(_.equipmentSearch).apply { model =>
      component(Props(ctx, model, onClick))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model, onClick) =>
    val items = model.value.items.map { i =>
      ItemComponent(ctx, i, onClick = model.value.initializedSlot _ andThen onClick)
    }
    InfiniteScroll(
      pageStart = 0,
      loadMore = Callback.empty,
      hasMore = model.value.total > model.value.items.length,
      loader = <.div(
        ^.style := js.Dynamic.literal("textAlign" -> "center"),
        ^.key := -1,
        ProgressCircular(indeterminate = true)),
      useWindow = false
    )(items: _*)
  }

  object ItemComponent {
    final case class Props(context: Context, item: ItemWithCachedEffects, onClick: Item => Callback)
    private implicit val reusability: Reusability[Props] = Reusability.by(_.item.value.id)

    def apply(ctx: Context, item: ItemWithCachedEffects, onClick: Item => Callback): VdomElement =
      component.withKey(item.value.id)(Props(ctx, item, onClick))

    private val component = ScalaComponent.builder[Props]("ItemComponent")
      .render_P { case Props(ctx, ItemWithCachedEffects(item, effectList), onClick) =>
        val outline = ItemRarity.withValueOpt(item.rarity).collect {
          case ItemRarity.Admin => Theme.admin
          case ItemRarity.Legendary => Theme.legendary
          case ItemRarity.Epic => Theme.epic
          case ItemRarity.PvP => Theme.pvp
          case ItemRarity.Relic => Theme.relic
          case ItemRarity.Mythical => Theme.mythical
          case ItemRarity.Rare => Theme.rare
          case ItemRarity.Common => Theme.common
        }
        <.div(
          Theme.itemRow,
          ^.onClick --> onClick(item),
          <.img(
            Theme.itemIcon,
            outline.whenDefined,
            ^.src := ctx.assets.makeItemIconLink(item.gfxId),
            ^.width := "32px",
            ^.height := "32px"
          ),
          <.div(Theme.itemName, item.name.map(VdomNode.cast).getOrElse(ctx.localization.ui("undefined_item"))),
          <.div(Theme.itemLevel, ctx.localization.ui("level_short"), " ", SlotState.initializedLevel(item)),
          effectList
        )
      }.configure(Reusability.shouldComponentUpdate)
      .build
  }
}
