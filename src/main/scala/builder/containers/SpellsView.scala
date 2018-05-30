package builder.containers

import builder._
import builder.containers.SpellsView.Route
import builder.raw.onsen.Navigator.NavHandler
import builder.raw.onsen._
import builder.state.SpellsState.SpellLens
import builder.state._
import builder.util.EffectList
import builder.util.EffectProcessor.Modifiers
import diode.FastEq
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import monocle.Optional
import org.bitbucket.wakfuthesaurus.shared.data.{CharacterBreed, RelValue, Spell}
import scalacss.ScalaCssReact._

import scala.scalajs.js
import scala.scalajs.js._

object SpellRow {
  final case class Props(
    context: Context,
    spells: Seq[Spell],
    taken: Set[Int],
    onClick: Spell => Callback
  )

  def apply(context: Context, key: Int, spells: Seq[Spell], onClick: Spell => Callback, isActive: Boolean): VdomElement =
    MainCircuit.connect(_.character.spells).apply { model =>
      val playerSpells = if (isActive) model.value.actives else model.value.passives
      val taken = playerSpells.flatMap(_.map(_.id)).toSet
      component.withKey(key)(Props(context, spells, taken, onClick))
    }

  def renderSpellEntry(ctx: Context, spell: Spell, blocked: Boolean, onClick: Callback): VdomElement =
    <.div(Theme.spellEntryContainer,
      ^.key := spell.id,
      ^.onClick --> CallbackOption.fromCallback(onClick).unless(blocked),
      ctx.assets.spellIcon(spell.gfxId, Some(if (blocked) Theme.inactiveSpellEntry else Theme.spellEntry))
    )

  private val component = ScalaFnComponent[Props] { case Props(ctx, spells, taken, onClick) =>
    <.div(Theme.spellRow,
      spells.sortBy(_.uiPosition).map { spell =>
        renderSpellEntry(ctx, spell, taken.contains(spell.id), onClick(spell))
      }.toVdomArray
    )
  }
}

object DeckView {
  final case class Props(
    context: Context,
    state: SpellsState,
    onClick: Boolean => SpellLens => Option[Spell] => Callback
  )

  def apply(context: Context, onClick: Boolean => SpellLens => Option[Spell] => Callback) =
    MainCircuit.connect(_.character.spells).apply { model =>
      component(Props(context, model.value, onClick))
    }

  def renderDeckRow(ctx: Context, spells: SpellsState, onClick: SpellLens => Option[Spell] => Callback)
    (lenses: Seq[Optional[SpellsState, Option[Spell]]]): VdomElement =
    <.div(Theme.deckRow,
      lenses.zipWithIndex.map(scala.Function.tupled(renderDeckEntry(ctx, spells, onClick))).toVdomArray
    )

  def renderDeckEntry(ctx: Context, spells: SpellsState, onClick: SpellLens => Option[Spell] => Callback)
    (lens: Optional[SpellsState, Option[Spell]], key: Int): VdomElement =
    <.div(Theme.deckEntry,
      ^.key := key,
      ^.onClick --> onClick(lens)(lens.getOption(spells).flatten),
      ctx.assets.icon("deck_slot.png", Some(Theme.deckSlot)),
      lens.getOption(spells).flatten.map { spell =>
        ctx.assets.spellIcon(spell.gfxId, Some(Theme.deckSpell))
      }.getOrElse(<.div(Theme.deckSpell))
    )

  def code(spells: SpellsState): String =
    spells.actives ::: spells.passives map(_.map(_.id).getOrElse(0)) mkString "-"

  private val component = ScalaFnComponent[Props] { case Props(ctx, state, onClick) =>
    <.div(Theme.deckContainer,
      <.div(Theme.deckHeader, ctx.localization.ui("active_skills")),
      renderDeckRow(ctx, state, onClick(true))(SpellsState.firstActiveRow),
      renderDeckRow(ctx, state, onClick(true))(SpellsState.secondActiveRow),
      <.div(Theme.deckHeader, ctx.localization.ui("passive_skills")),
      renderDeckRow(ctx, state, onClick(false))(SpellsState.passiveRow),
      <.div(Theme.deckHeader, ctx.localization.ui("code_deck")),
      <.input(Theme.deckCode, ^.disabled := true, ^.value := code(state))
    )
  }
}

object SpellsView {
  import ClassDataState.fastEq

  final case class Props(
    context: Context,
    model: ModelProxy[ClassDataState]
  )

  def apply(key: Int, context: Context): VdomElement =
    MainCircuit.connect(_.classData).withKey(key).apply { model =>
      component.withKey(key)(Props(context, model))
    }

  sealed abstract class Route(val translationKey: String)
  object Route {
    case object Deck extends Route("deck")
    final case class ActiveChoice(lens: SpellLens) extends Route("actives")
    final case class PassiveChoice(lens: SpellLens) extends Route("passives")
    final case class Spell(
      spell: org.bitbucket.wakfuthesaurus.shared.data.Spell,
      lens: SpellLens,
      active: Boolean
    ) extends Route("spell")
  }

  def renderToolbar(ctx: Context, route: Route, nav: NavHandler[Route]): () => VdomElement = () =>
    route match {
      case Route.Deck =>
        Toolbar(modifier = "transparent")(
          <.div(^.className := "center", ctx.localization.ui(route.translationKey)))
      case _ =>
        Toolbar(modifier = "transparent")(
          <.div(^.className := "left")(BackButton(onClick = () => Callback(nav.popPage()))),
          <.div(^.className := "center", ctx.localization.ui(route.translationKey))
        )
    }

  def renderPage(ctx: Context, breed: CharacterBreed): PartialFunction[(Route, NavHandler[Route]), VdomElement] = {
    case (Route.Deck, nav) =>
      def deckRoute(isActive: Boolean)(lens: SpellLens): Option[Spell] => Callback = {
        case Some(spell) => Callback {
          nav.pushPage(Route.Spell(spell, lens, isActive))
        }
        case None if isActive => Callback {
          nav.pushPage(Route.ActiveChoice(lens))
        }
        case None => Callback {
          nav.pushPage(Route.PassiveChoice(lens))
        }
      }
      Page(renderToolbar = renderToolbar(ctx, Route.Deck, nav))(DeckView(ctx, deckRoute))
    case (route@Route.PassiveChoice(lens), nav) =>
      Page(renderToolbar = renderToolbar(ctx, route, nav))(
        SpellRow(ctx, -1, breed.passive, spell => Callback(nav.pushPage(Route.Spell(spell, lens, active = false))), isActive = false))
    case (route@Route.ActiveChoice(lens), nav) =>
      Page(renderToolbar = renderToolbar(ctx, route, nav))(
        <.div(Theme.deckContainer,
          <.div(Theme.deckHeader, ctx.localization.ui("elemental_spells")),
          breed.spells.groupBy(_.element).map { case (elem, spellsOfElem) =>
            SpellRow(ctx, elem, spellsOfElem, spell => Callback(nav.pushPage(Route.Spell(spell, lens, active = true))), isActive = true)
          }.toVdomArray,
          <.div(Theme.deckHeader, ctx.localization.ui("support_spells")),
          SpellRow(ctx, -2, breed.support, spell => Callback(nav.pushPage(Route.Spell(spell, lens, active = true))), isActive = true)
        )
      )()
    case (route: Route.Spell, nav) =>
      SpellView(ctx, nav, route)
  }

  private val component = ScalaFnComponent[Props] { case Props(ctx, cls) =>
    cls.value match {
      case ClassDataState.Loaded(breed) =>
        Navigator[Route](
          swipeable = false,
          renderPage = renderPage(ctx, breed),
          initialRoute = Route.Deck
        )
      case ClassDataState.Loading =>
        <.div
    }
  }
}

object SpellView {
  final case class Props(
    context: Context,
    model: ModelProxy[CharacterLevelState],
    nav: NavHandler[Route],
    route: Route.Spell
  )

  def apply(
    context: Context,
    nav: NavHandler[Route],
    route: Route.Spell
  ): VdomElement =
    MainCircuit.connect(_.character.level).apply { model =>
      component(Props(context, model, nav, route))
    }

  def realValue(rel: RelValue, level: Int): Int =
    rel.base + rel.inc * level toInt

  def renderSpellInfo(ctx: Context, spell: Spell, level: Int): VdomElement =
    <.div(Theme.spellInfoRow,
      renderSpellInfoEntry(ctx, "stats/ap.png", realValue(spell.apCost, level)),
      renderSpellInfoEntry(ctx, "stats/mp.png", realValue(spell.mpCost, level)),
      renderSpellInfoEntry(ctx, "stats/wp.png", realValue(spell.wpCost, level)),
      renderRangeInfo(ctx, spell, level),
      renderSpellInfoEntry(ctx, "spell/line.png", spell.requireLine),
      renderSpellInfoEntry(ctx, "spell/diagonal.png", spell.requireDiagonal),
      renderSpellInfoEntry(ctx, "spell/mod_range.png", spell.modifiableRange),
      renderSpellInfoEntry(ctx, "spell/no_los.png", !spell.requireLos),
      renderSpellInfoEntry(ctx, "spell/st.png", spell.singleTarget),
      renderSpellInfoEntry(ctx, "spell/aoe.png", !spell.singleTarget)
    )

  def renderSpellInfoEntry(ctx: Context, icon: String, value: Boolean): TagMod =
    <.div(Theme.spellInfoEntry,
      ^.key := icon,
      ctx.assets.icon(icon, Some(Theme.spellInfoIcon))
    ).when(value)

  def renderSpellInfoEntry(ctx: Context, icon: String, value: Int): TagMod =
    <.div(Theme.spellInfoEntry,
      ^.key := icon,
      ctx.assets.icon(icon, Some(Theme.spellInfoIcon)),
      value
    ).when(value > 0)

  def renderRangeInfo(ctx: Context, spell: Spell, level: Int): TagMod = {
    val minRange = realValue(spell.minRange, level)
    val maxRange = realValue(spell.maxRange, level)
    <.div(Theme.spellInfoEntry,
      ctx.assets.icon("spell/range.png", Some(Theme.spellInfoIcon)),
      if (minRange == maxRange) minRange
      else <.span(minRange, "-", maxRange)
    )
  }

  def renderToolbar(
    ctx: Context,
    route: Route.Spell,
    nav: NavHandler[Route]
  ): () => VdomElement = () =>
    Toolbar(modifier = "transparent")(
      <.div(^.className := "left")(
        BackButton(onClick = () => Callback(nav.popPage()))
      ),
      <.div(^.className := "center",
        Theme.toolbarHeader,
        ctx.assets.spellIcon(route.spell.gfxId, Some(Theme.toolbarHeaderIcon)),
        route.spell.name
      ),
      SpellUseButton(ctx, route)
    )

  private val component = ScalaFnComponent[Props] { case Props(ctx, model, nav, route) =>
    Page(renderToolbar = renderToolbar(ctx, route, nav))(
      renderSpellInfo(ctx, route.spell, model.value.level).when(route.active),
      SpellEffectView(ctx, route.spell, route.active),
      if (util.common.isTouchDevice) SpellToast(ctx) else EmptyVdom,
      Fab(onClick = () => Callback.lazily {
        val opts = js.Dynamic.literal(
          "animationOptions" ->
            js.Dynamic.literal(
              "duration" -> 0.2,
              "delay" -> 0
            )
        )
        val nextPop: js.Function1[js.Any, js.Any | Thenable[js.Any]] = _ => nav.popPage(opts)
        Callback(nav.popPage(opts).then(onFulfilled = nextPop))
      }, position = "bottom right")(
        Icon(icon = "angle-double-left",
          style = js.Dynamic.literal(
            "fontSize" -> "40px",
            "marginLeft" -> "-2px"
          )
        )
      )
    )()
  }
}

object SpellToast {
  final case class Props(
    context: Context,
    model: ModelProxy[SpellToastState]
  )

  def apply(context: Context): VdomElement =
    MainCircuit.connect(_.spellToast).apply { model =>
      component(Props(context, model))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    Toast(isOpen = model.value.toastText.isDefined)(
      <.div(^.className := "message")(model.value.toastText.fold("")(identity))
    )
  }
}

object SpellUseButton {
  final case class Props(
    context: Context,
    model: ModelProxy[SpellsState],
    route: Route.Spell
  )

  def apply(context: Context, route: Route.Spell): VdomElement =
    MainCircuit.connect(_.character.spells).apply { model =>
      component(Props(context, model, route))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model, route) =>
    val exists = (model.value.passives ::: model.value.actives).flatMap(_.map(_.id)).contains(route.spell.id)
    if (exists) {
      <.div(Theme.spellUseContainer)(
        Button(onClick = () => model.dispatchCB(SpellUsed(route.spell, route.lens, added = false)))(
          ctx.localization.ui("remove")
        )
      )
    } else {
      <.div(Theme.spellUseContainer)(
        Button(onClick = () => model.dispatchCB(SpellUsed(route.spell, route.lens, added = true)))(
          ctx.localization.ui("use")
        )
      )
    }
  }
}

object SpellEffectView {
  final case class Props(
    context: Context,
    model: ModelProxy[(CharacterLevelState, Characteristics)],
    spell: Spell,
    isActive: Boolean
  )

  final case class State(view: Int)

  def apply(context: Context, spell: Spell, isActive: Boolean): VdomElement =
    MainCircuit.connect(s => (s.character.level, s.characteristics))(FastEq.ValueEq).apply { character =>
      component(Props(context, character, spell, isActive))
    }

  def renderNormalEffects(ctx: Context, maybeMods: Option[Modifiers], spell: Spell, level: Int): VdomNode = {
    <.div(Theme.spellEffectsContainer,
      <.div(Theme.spellEffectsHeader, ctx.localization.ui("base")),
      EffectList.renderList(ctx)(spell.effects, level, None, Some(Theme.spellEffects)),
      maybeMods.map { mods =>
        <.div(
          <.div(Theme.spellEffectsHeader, ctx.localization.ui("preview")),
          EffectList.renderList(ctx)(spell.effects, level, Some(mods), Some(Theme.spellEffects))
        )
      }
    )
  }

  def renderCriticalEffects(ctx: Context, modifiers: Modifiers, spell: Spell, level: Int): VdomNode = {
    <.div(Theme.spellEffectsContainer,
      <.div(Theme.spellEffectsHeader, ctx.localization.ui("base")),
      EffectList.renderList(ctx)(
        if (spell.criticalEffects.isEmpty) spell.effects else spell.criticalEffects,
        level, None, Some(Theme.spellEffects)
      ),
      <.div(Theme.spellEffectsHeader, ctx.localization.ui("preview")),
      EffectList.renderList(ctx)(
        if (spell.criticalEffects.isEmpty) spell.effects else spell.criticalEffects,
        level, Some(modifiers.copy(isCritical = true)), Some(Theme.spellEffects)
      )
    )
  }

  def renderConditions(ctx: Context, spell: Spell, level: Int): VdomNode = {
    val maxCastPerTurnValue = SpellView.realValue(spell.maxCastPerTurn, level)
    val maxCastPerTurn =
      if (maxCastPerTurnValue > 0)
        <.li(^.key := "use_per_turn", maxCastPerTurnValue, " ", ctx.localization.ui("use_per_turn")) :: Nil
      else Nil
    val maxCastPerTarget =
      if (spell.maxCastPerTarget > 0)
        <.li(^.key := "use_per_target", spell.maxCastPerTarget, " ", ctx.localization.ui("use_per_target")) :: Nil
      else Nil
    val requirements = maxCastPerTurn ::: maxCastPerTarget
    if (requirements.isEmpty)
      <.p(Theme.noRequirements, ctx.localization.ui("none"))
    else
      <.ul(Theme.spellRequirements, requirements.toVdomArray)
  }

  def renderTab(props: Props, view: Int): VdomNode = {
    val (CharacterLevelState(level), characs) = props.model.value
    val modifiers = Modifiers(
      characs,
      resistance = 0,
      isArea = !props.spell.singleTarget,
      isCritical = false,
      isDistance = false,
      isBackstab = false,
      isBerserk = false
    )
    view match {
      case 0 =>
        renderNormalEffects(props.context, Some(modifiers), props.spell, level)
      case 1 =>
        renderCriticalEffects(props.context, modifiers, props.spell, level)
      case 2 =>
        renderConditions(props.context, props.spell, level)
    }
  }

  private val component = ScalaComponent.builder[Props]("SpellEffectView")
    .initialState(State(0))
    .render { $ =>
      if ($.props.isActive) {
        <.div(Theme.spellSegment,
          Segment(
            index = $.state.view,
            onPostChange = i => $.modState(_.copy(view = i.index)))(
            <.button($.props.context.localization.ui("normal")),
            <.button($.props.context.localization.ui("critical")),
            <.button($.props.context.localization.ui("conditions"))
          ),
          <.div(Theme.spellTabContainer, renderTab($.props, $.state.view))
        )
      } else {
        val skillLevel = $.props.spell.upgrades.takeWhile(_ <= $.props.model.value._1.level).length
        <.div(Theme.spellTabContainer,
          renderNormalEffects($.props.context, None, $.props.spell, skillLevel)
        )
      }
    }
    .build
}