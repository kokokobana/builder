package builder.containers

import builder._
import builder.raw.onsen.Icon
import builder.state.Skills._
import builder.state.{CharacterLevelState, Skills}
import cats.implicits._
import diode.FastEq
import diode.react.ModelProxy
import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.{Reusability, StateSnapshot}
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import monocle.Lens
import scalacss.ScalaCssReact._

object SkillRow {
  final case class Props(
    context: Context,
    translationKey: String,
    state: StateSnapshot[Int],
    max: Int,
    remaining: Int
  )

  implicit val reusability: Reusability[Props] = Reusability.by { props =>
    (props.state.value, props.remaining)
  }

  def apply(
    translationKey: String,
    state: StateSnapshot[Int],
    max: Int
  )(context: Context,
    key: Int,
    remaining: Int
  ): VdomElement = component.withKey(key)(Props(context, translationKey, state, max, remaining))

  private val component = ScalaComponent.builder[Props]("SkillRow")
    .render_P { case Props(ctx, translationKey, state, max, remaining) =>
      <.li(Theme.skillRow,
        <.span(Theme.skillName, ctx.localization.skill(translationKey)),
        <.div(Theme.skillRowIcon,
          ^.onClick --> state.setState(0),
          Icon(icon = "md-square-o", size = 32, color = "white")
        ),
        <.div(Theme.skillRowIcon,
          ^.onClick --> state.modState(i => math.max(0, i - 1)),
          Icon(icon = "md-minus-square", size = 32, color = "white")
        ),
        <.span(if (max === state.value) Theme.maxSkillValue else Theme.skillValue,
          state.value.toString
        ),
        <.div(Theme.skillRowIcon,
          ^.onClick --> state.modState(i => math.min(max, i + math.min(remaining, 1))),
          Icon(icon = "md-plus-square", size = 32, color = "white")
        ),
        <.div(Theme.skillRowIcon,
          ^.onClick --> state.modState(i => math.min(max, i + math.min(remaining, 5))),
          Icon(icon = "md-n-5-square", size = 32, color = "white")
        )
      )
    }.configure(Reusability.shouldComponentUpdate)
    .build
}

object SkillsView {

  type ToSkillRow = (Context, Int, Int) => VdomElement

  final case class Props(context: Context, model: ModelProxy[(Skills, CharacterLevelState)])
  final case class SkillGroup(translationKey: String, skills: Seq[ToSkillRow], remaining: Int)

  def intelligence(intelligence: StateSnapshot[Intelligence], level: Int): SkillGroup =
    SkillGroup(
      translationKey = "intelligence",
      skills = Seq(
        SkillRow("%_health_points", intelligence.zoomStateL(Intelligence.percentHealthPoints), max = 1000),
        SkillRow("elemental_resist", intelligence.zoomStateL(Intelligence.elementalResist), max = 10),
        SkillRow("barrier", intelligence.zoomStateL(Intelligence.barrier), max = 10),
        SkillRow("%_heals_received", intelligence.zoomStateL(Intelligence.healsReceived), max = 5),
        SkillRow("%_armor_health_points", intelligence.zoomStateL(Intelligence.armorHealthPoints), max = 10)
      ),
      remaining = Intelligence.remaining(intelligence.value)(level)
    )

  def strength(strength: StateSnapshot[Strength], level: Int): SkillGroup =
    SkillGroup(
      translationKey = "strength",
      skills = Seq(
        SkillRow("elemental_mastery", strength.zoomStateL(Strength.elementalMastery), max = 1000),
        SkillRow("st_mastery", strength.zoomStateL(Strength.singleTargetMastery), max = 20),
        SkillRow("area_mastery", strength.zoomStateL(Strength.areaMastery), max = 20),
        SkillRow("cc_mastery", strength.zoomStateL(Strength.closeCombatMastery), max = 20),
        SkillRow("distance_mastery", strength.zoomStateL(Strength.distanceMastery), max = 20),
        SkillRow("health_points", strength.zoomStateL(Strength.healthPoints), max = 1000)
      ),
      remaining = Strength.remaining(strength.value)(level)
    )

  def agility(agility: StateSnapshot[Agility], level: Int): SkillGroup =
    SkillGroup(
      translationKey = "agility",
      skills = Seq(
        SkillRow("lock", agility.zoomStateL(Agility.lock), max = 1000),
        SkillRow("dodge", agility.zoomStateL(Agility.dodge), max = 1000),
        SkillRow("initiative", agility.zoomStateL(Agility.initiative), max = 20),
        SkillRow("lock_and_dodge", agility.zoomStateL(Agility.lockAndDodge), max = 1000),
        SkillRow("%_ap_mp_removal", agility.zoomStateL(Agility.apAndMpRemoval), max = 20),
        SkillRow("%_ap_mp_resist", agility.zoomStateL(Agility.apAndMpResist), max = 20)
      ),
      remaining = Agility.remaining(agility.value)(level)
    )

  def chance(chance: StateSnapshot[Chance], level: Int): SkillGroup =
    SkillGroup(
      translationKey = "chance",
      skills = Seq(
        SkillRow("%_crit_chance", chance.zoomStateL(Chance.criticalHits), max = 20),
        SkillRow("%_block", chance.zoomStateL(Chance.block), max = 20),
        SkillRow("crit_mastery", chance.zoomStateL(Chance.criticalMastery), max = 1000),
        SkillRow("rear_mastery", chance.zoomStateL(Chance.rearMastery), max = 1000),
        SkillRow("berserk_mastery", chance.zoomStateL(Chance.berserkMastery), max = 1000),
        SkillRow("heal_mastery", chance.zoomStateL(Chance.healingMastery), max = 1000),
        SkillRow("rear_resist", chance.zoomStateL(Chance.rearResist), max = 20),
        SkillRow("crit_resist", chance.zoomStateL(Chance.criticalResist), max = 20)
      ),
      remaining = Chance.remaining(chance.value)(level)
    )

  def major(major: StateSnapshot[Major], level: Int): SkillGroup =
    SkillGroup(
      translationKey = "major",
      skills = Seq(
        SkillRow("ap", major.zoomStateL(Major.ap), max = 1),
        SkillRow("mp", major.zoomStateL(Major.mpAndDamage), max = 1),
        SkillRow("range", major.zoomStateL(Major.rangeAndDamage), max = 1),
        SkillRow("wp", major.zoomStateL(Major.wp), max = 1),
        SkillRow("control", major.zoomStateL(Major.controlAndDamage), max = 1),
        SkillRow("%_damage_inflicted", major.zoomStateL(Major.damageInflicted), max = 1),
        SkillRow("major_resist", major.zoomStateL(Major.majorResistance), max = 1)
      ),
      remaining = Major.remaining(major.value)(level)
    )

  def skillGroups(state: ModelProxy[Skills], level: Int): Seq[SkillGroup] =
    Seq(
      intelligence(mkSnapshot(state, Skills.intelligence), level),
      strength(mkSnapshot(state, Skills.strength), level),
      agility(mkSnapshot(state, Skills.agility), level),
      chance(mkSnapshot(state, Skills.chance), level),
      major(mkSnapshot(state, Skills.major), level)
    )

  def mkSnapshot[T](model: ModelProxy[Skills], lens: Lens[Skills, T]): StateSnapshot[T] =
    StateSnapshot(lens.get(model.value)) { (maybeState, _) =>
      CallbackOption.liftOption(maybeState).flatMap(sv => model.dispatchCB(UpdateSkills(lens.set(sv))))
    }

  def apply(context: Context): VdomElement =
    MainCircuit.connect(st => (st.character.skills, st.character.level))(FastEq.ValueEq).apply { model =>
      component(Props(context, model))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    <.div(
      skillGroups(model.zoom(_._1), model.value._2.level)
        .zipWithIndex.map(Function.tupled(renderSkillGroup(ctx)))
        .toVdomArray
    )
  }

  def renderSkillGroup(ctx: Context)(group: SkillGroup, key: Int): VdomElement =
    <.div(
      ^.key := key,
      <.div(Theme.skillGroupHeader,
        <.div(Theme.skillGroupTitle, ctx.localization.ui(group.translationKey)),
        <.span(Theme.skillGroupPoints, group.remaining, " ", ctx.localization.ui("points"))
      ),
      <.ul(Theme.skillGroup,
        group.skills.zipWithIndex.map { case (skill, i) =>
          skill(ctx, i, group.remaining)
        }.toVdomArray
      )
    )
}
