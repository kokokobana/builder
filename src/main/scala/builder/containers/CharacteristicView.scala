package builder.containers

import builder.state.Characteristics
import builder.{Context, MainCircuit, Theme}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js
import scalacss.ScalaCssReact._

object CharacteristicView {
  final case class Props(context: Context, characteristics: ModelProxy[Characteristics])

  def apply(context: Context) =
    MainCircuit.connect(_.characteristics).apply(m => component(Props(context, m)))

  private def renderHead(colspan: Int, title: VdomNode): VdomElement =
    <.thead(<.tr(<.th(Theme.characteristicHeader, ^.colSpan := colspan, title)))

  private def renderElement(
    ctx: Context,
    mastery: Int,
    resist: Int,
    icon: String
  ): VdomElement = {
    val finalRes = math.floor((1 - math.pow(0.8, resist / 100.0)) * 100)
    <.td(
      Theme.characteristicCell,
      <.div(
        Theme.masteryContent,
        ctx.assets.icon(s"elements/$icon.png", Some(Theme.masteryIcon)),
        ElementalCharacteristicValue(ctx, mastery.toString, "mastery"),
        ElementalCharacteristicValue(ctx, s"$finalRes% ($resist)", "block"),
      )
    )
  }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    val characs = model.value
    <.div(
      <.table(
        Theme.characteristicTable,
        <.tbody(
          <.tr(
            CharacteristicCell(ctx, characs.hp, main = true, icon = "hp", labelKey = "hp"),
            ArmorCell(ctx, characs.hp),
            CharacteristicCell(ctx, characs.ap, width = Some(50 / 3.0), main = true, icon = "ap", labelKey = "ap"),
            CharacteristicCell(ctx, characs.mp, width = Some(50 / 3.0), main = true, icon = "mp", labelKey = "mp"),
            CharacteristicCell(ctx, characs.wp, width = Some(50 / 3.0), main = true, icon = "wp", labelKey = "wp")
          )
        )
      ),
      <.table(
        Theme.characteristicTable,
        renderHead(2, ctx.localization.ui("mastery")),
        <.tbody(
          <.tr(
            renderElement(ctx, characs.waterMastery, characs.waterResist, "water"),
            renderElement(ctx, characs.earthMastery, characs.earthResist, "earth")),
          <.tr(
            renderElement(ctx, characs.airMastery, characs.airResist, "air"),
            renderElement(ctx, characs.fireMastery, characs.fireResist, "fire")
          )
        )
      ),
      <.table(
        Theme.characteristicTable,
        renderHead(2, ctx.localization.ui("battle")),
        <.tbody(
          <.tr(
            CharacteristicCell(ctx, characs.damageInflicted, "damage_dealt", "damage_dealt"),
            CharacteristicCell(ctx, characs.healsPerformed, "heals", "heals_performed")
          ),
          <.tr(
            CharacteristicCell(ctx, characs.criticalHits, "ch", "crit_chance"),
            CharacteristicCell(ctx, characs.block, "block", "block")
          ),
          <.tr(
            CharacteristicCell(ctx, characs.initiative, "initiative", "initiative"),
            CharacteristicCell(ctx, characs.range, "range", "range")
          ),
          <.tr(
            CharacteristicCell(ctx, characs.dodge, "dodge", "dodge"),
            CharacteristicCell(ctx, characs.lock, "lock", "lock")
          ),
          <.tr(
            CharacteristicCell(ctx, characs.wisdom, "wisdom", "wisdom"),
            CharacteristicCell(ctx, characs.prospecting, "prospecting", "prospecting")
          ),
          <.tr(
            CharacteristicCell(ctx, characs.control, "control", "control"),
            CharacteristicCell(ctx, characs.kitSkill, "kit_skill", "kit_skill")
          )
        )
      ),
      <.table(
        Theme.characteristicTable,
        renderHead(2, ctx.localization.ui("secondary")),
        <.tbody(
          <.tr(
            CharacteristicCell(ctx, characs.criticalMastery, "crit_mastery", "crit_mastery"),
            CharacteristicCell(ctx, characs.criticalResist, "crit_resist", "crit_resist")
          ),
          <.tr(
            CharacteristicCell(ctx, characs.rearMastery, "rear_mastery", "rear_mastery"),
            CharacteristicCell(ctx, characs.rearResist, "rear_resist", "rear_resist")
          ),
          <.tr(
            CharacteristicCell(ctx, characs.meleeMastery, "melee_mastery", "melee_mastery"),
            CharacteristicCell(ctx, characs.distanceMastery, "distance_mastery", "distance_mastery")
          ),
          <.tr(
            CharacteristicCell(ctx, characs.singleTargetMastery, "st_mastery", "st_mastery"),
            CharacteristicCell(ctx, characs.areaMastery, "area_mastery", "area_mastery")
          ),
          <.tr(
            CharacteristicCell(ctx, characs.healMastery, "heals", "heal_mastery"),
            CharacteristicCell(ctx, characs.berserkMastery, "berserk_mastery", "berserk_mastery")
          )
        )
      )
    )
  }

  object CharacteristicCell {
    final case class Props(
      context: Context,
      value: Int,
      icon: String,
      labelKey: String,
      width: Option[Double] = None,
      main: Boolean = false
    )
    private implicit val reusability: Reusability[Props] = Reusability.by(_.value)

    def apply(
      context: Context,
      value: Int,
      icon: String,
      labelKey: String,
      width: Option[Double] = None,
      main: Boolean = false
    ): Unmounted[Props, Unit, Unit] = component(Props(context, value, icon, labelKey, width, main))

    private val component = ScalaComponent.builder[Props]("CharacteristicCell")
      .render_P { case Props(ctx, value, icon, labelKey, width, isMain) =>
        val style = width.map(w => js.Dynamic.literal("width" -> s"$w%"))
        <.td(
          Theme.characteristicCell,
          ^.style :=? style,
          <.div(
            Theme.characteristicContent,
            ctx.assets.icon(s"stats/$icon.png", Some(Theme.characteristicImage)),
            <.div(
              if (isMain) Theme.mainCharacteristicName
              else Theme.characteristicName,
              ctx.localization.characteristic(labelKey)
            ),
            value match {
              case i if i > 0 =>
                <.div(Theme.characteristicPositive, i)
              case i if i < 0 =>
                <.div(Theme.characteristicNegative, i)
              case i =>
                <.div(Theme.characteristicNeutral, i)
            }
          )
        )
      }.configure(Reusability.shouldComponentUpdate)
      .build
  }

  object ElementalCharacteristicValue {
    final case class Props(context: Context, value: String, icon: String)
    private implicit val reusability: Reusability[Props] = Reusability.by(_.value)

    def apply(context: Context, value: String, icon: String): Unmounted[Props, Unit, Unit] =
      component(Props(context, value, icon))

    private val component = ScalaComponent.builder[Props]("ElementalCharacteristicValue")
      .render_P { case Props(ctx, value, icon) =>
        <.div(
          Theme.masteryCharacteristic,
          ctx.assets.icon(s"stats/$icon.png"),
          <.span(Theme.masteryValue, value)
        )
      }.configure(Reusability.shouldComponentUpdate)
      .build
  }

  object ArmorCell {
    final case class Props(context: Context)

    def apply(context: Context, hp: Int): VdomElement =
      MainCircuit.connect(_.character.skills.intelligence).apply { m =>
        CharacteristicCell(context, (hp * m.value.armorHealthPoints * 0.04).toInt, "armor", "armor", main = true)
      }
  }

}