package builder.containers

import builder.raw.onsen.Input
import builder.state.CustomCharacteristics
import builder.{Context, MainCircuit, Theme, UpdateCustomCharacteristics}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import monocle.Lens
import org.scalajs.dom.html.Input
import scalacss.ScalaCssReact._

import scala.scalajs.js

object CharacteristicCustomization {
  final case class Props(context: Context, characteristics: ModelProxy[CustomCharacteristics])

  def apply(context: Context) =
    MainCircuit.connect(_.character.customCharacteristics).apply(m => component(Props(context, m)))

  private def renderHead(colspan: Int, title: VdomNode): VdomElement =
    <.thead(<.tr(<.th(Theme.characteristicHeader, ^.colSpan := colspan, title)))

  private def renderElement(
    ctx: Context,
    model: ModelProxy[CustomCharacteristics],
    mastery: Lens[CustomCharacteristics, Int],
    resist: Lens[CustomCharacteristics, Int],
    icon: String
  ): VdomElement = {
    <.td(
      Theme.characteristicCell,
      <.div(
        Theme.masteryContent,
        ctx.assets.icon(s"elements/$icon.png", Some(Theme.masteryIcon)),
        ElementalCharacteristicCell(
          ctx,
          mastery.get(model.value),
          i => model.dispatchCB(UpdateCustomCharacteristics(mastery.set(i))),
          "mastery"
        ),
        ElementalCharacteristicCell(
          ctx,
          resist.get(model.value),
          i => model.dispatchCB(UpdateCustomCharacteristics(resist.set(i))),
          "block"
        )
      )
    )
  }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    val characs = model.value

    def renderCell(
      lens: Lens[CustomCharacteristics, Int],
      icon: String,
      labelKey: String,
      main: Boolean = false
    ) = CharacteristicCell(
      ctx,
      lens.get(characs),
      i => model.dispatchCB(UpdateCustomCharacteristics(lens.set(i))),
      icon,
      labelKey,
      main
    )

    <.div(
      <.table(
        Theme.characteristicTable,
        <.tbody(
          <.tr(
            renderCell(CustomCharacteristics.hp, main = true, icon = "hp", labelKey = "hp"),
            renderCell(CustomCharacteristics.ap, main = true, icon = "ap", labelKey = "ap"),
            renderCell(CustomCharacteristics.mp, main = true, icon = "mp", labelKey = "mp"),
            renderCell(CustomCharacteristics.wp, main = true, icon = "wp", labelKey = "wp")
          )
        )
      ),
      <.table(
        Theme.characteristicTable,
        renderHead(2, ctx.localization.ui("mastery")),
        <.tbody(
          <.tr(
            renderElement(ctx, model, CustomCharacteristics.waterMastery, CustomCharacteristics.waterResist, "water"),
            renderElement(ctx, model, CustomCharacteristics.earthMastery, CustomCharacteristics.earthResist, "earth")),
          <.tr(
            renderElement(ctx, model, CustomCharacteristics.airMastery, CustomCharacteristics.airResist, "air"),
            renderElement(ctx, model, CustomCharacteristics.fireMastery, CustomCharacteristics.fireResist, "fire")
          )
        )
      ),
      <.table(
        Theme.characteristicTable,
        renderHead(2, ctx.localization.ui("battle")),
        <.tbody(
          <.tr(
            renderCell(CustomCharacteristics.damageInflicted, "damage_dealt", "damage_dealt"),
            renderCell(CustomCharacteristics.healsPerformed, "heals", "heals_performed")
          ),
          <.tr(
            renderCell(CustomCharacteristics.criticalHits, "ch", "crit_chance"),
            renderCell(CustomCharacteristics.block, "block", "block")
          ),
          <.tr(
            renderCell(CustomCharacteristics.initiative, "initiative", "initiative"),
            renderCell(CustomCharacteristics.range, "range", "range")
          ),
          <.tr(
            renderCell(CustomCharacteristics.dodge, "dodge", "dodge"),
            renderCell(CustomCharacteristics.lock, "lock", "lock")
          ),
          <.tr(
            renderCell(CustomCharacteristics.wisdom, "wisdom", "wisdom"),
            renderCell(CustomCharacteristics.prospecting, "prospecting", "prospecting")
          ),
          <.tr(
            renderCell(CustomCharacteristics.control, "control", "control"),
            renderCell(CustomCharacteristics.kitSkill, "kit_skill", "kit_skill")
          )
        )
      ),
      <.table(
        Theme.characteristicTable,
        renderHead(2, ctx.localization.ui("secondary")),
        <.tbody(
          <.tr(
            renderCell(CustomCharacteristics.criticalMastery, "crit_mastery", "crit_mastery"),
            renderCell(CustomCharacteristics.criticalResist, "crit_resist", "crit_resist")
          ),
          <.tr(
            renderCell(CustomCharacteristics.rearMastery, "rear_mastery", "rear_mastery"),
            renderCell(CustomCharacteristics.rearResist, "rear_resist", "rear_resist")
          ),
          <.tr(
            renderCell(CustomCharacteristics.meleeMastery, "melee_mastery", "melee_mastery"),
            renderCell(CustomCharacteristics.distanceMastery, "distance_mastery", "distance_mastery")
          ),
          <.tr(
            renderCell(CustomCharacteristics.singleTargetMastery, "st_mastery", "st_mastery"),
            renderCell(CustomCharacteristics.areaMastery, "area_mastery", "area_mastery")
          ),
          <.tr(
            renderCell(CustomCharacteristics.healMastery, "heals", "heal_mastery"),
            renderCell(CustomCharacteristics.berserkMastery, "berserk_mastery", "berserk_mastery")
          )
        )
      )
    )
  }

  object CharacteristicCell {
    final case class Props(
      context: Context,
      value: Int,
      onChange: Int => Callback,
      icon: String,
      labelKey: String,
      main: Boolean = false
    )

    def apply(
      context: Context,
      value: Int,
      onChange: Int => Callback,
      icon: String,
      labelKey: String,
      main: Boolean = false
    ): VdomElement = component(Props(context, value, onChange, icon, labelKey, main))

    implicit val reusability: Reusability[Props] = Reusability.by(_.value)

    private val component = ScalaComponent.builder[Props]("CharacteristicCell")
      .render_P { case Props(ctx, value, onChange, icon, labelKey, isMain) =>
        <.td(
          Theme.characteristicCell,
          <.div(
            Theme.characteristicContent,
            ctx.assets.icon(s"stats/$icon.png", Some(Theme.characteristicImage)),
            <.div(
              if (isMain) Theme.mainCharacteristicName
              else Theme.characteristicName,
              ctx.localization.characteristic(labelKey)
            ),
            <.div(Theme.characteristicInput,
              Input(
                `type` = "number",
                value = value.toString,
                onChange = js.defined { ev => onChange(ev.target.asInstanceOf[Input].valueAsNumber.toInt) }
              )
            )
          )
        )
      }.configure(Reusability.shouldComponentUpdate)
      .build
  }

  object ElementalCharacteristicCell {
    final case class Props(
      context: Context,
      value: Int,
      onChange: Int => Callback,
      icon: String
    )

    def apply(context: Context, value: Int, onChange: Int => Callback, icon: String): VdomElement =
      component(Props(context, value, onChange, icon))

    implicit val reusability: Reusability[Props] = Reusability.by(_.value)

    private val component = ScalaComponent.builder[Props]("ElementalCharacteristicValue")
      .render_P { case Props(ctx, value, onChange, icon) =>
        <.div(
          Theme.masteryCharacteristic,
          ctx.assets.icon(s"stats/$icon.png"),
          <.div(Theme.characteristicInput,
            Input(
              `type` = "number",
              value = value.toString,
              onChange = js.defined { ev => onChange(ev.target.asInstanceOf[Input].valueAsNumber.toInt) }
            )
          )
        )
      }.configure(Reusability.shouldComponentUpdate)
      .build
  }

}