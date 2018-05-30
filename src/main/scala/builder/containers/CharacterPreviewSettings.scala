package builder.containers
import builder._
import builder.components.ColorPicker
import builder.state._
import diode.FastEq
import diode.react.ModelProxy
import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.StateSnapshot
import japgolly.scalajs.react.raw.SyntheticMouseEvent
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import org.bitbucket.wakfuthesaurus.shared.data.{CharacterClass, CharacterSex, Color}
import org.scalajs.dom.raw.HTMLSelectElement
import scalacss.ScalaCssReact._

object CharacterPreviewSettings {
  final case class Props(ctx: Context, model: ModelProxy[(ClassDataState, AppearanceState)])

  def apply(context: Context): VdomElement =
    MainCircuit.connect(m => (m.classData, m.character.appearance))(FastEq.ValueEq).apply { model =>
      component(Props(context, model))
    }

  private val component = ScalaComponent.builder[Props]("CharacterPreviewSettings")
    .render_P { case Props(ctx, model) =>
    val (clazz, appearance) = model.value
    val snapshot = StateSnapshot(appearance) { (v, _) =>
      CallbackOption.liftOption(v).flatMap(app => model.dispatchCB(SetAppearance(app, silent = false)))
    }
    clazz.value match {
      case Some(breed) =>
        val cls = CharacterClass.withValue(breed.id)
        val df = breed.definitions.find(_.sex == appearance.sex.value).get
        VdomArray(
          renderSexSelect(ctx, snapshot.zoomStateL(AppearanceState.sex)),
          renderColorPicker(
            ctx.localization.ui("pick_skin_color"),
            snapshot.zoomStateL(AppearanceState.skinColor),
            df.skinColors
          ),
          renderColorPicker(
            ctx.localization.ui("pick_hair_color"),
            snapshot.zoomStateL(AppearanceState.hairColor),
            df.hairColors
          ),
          renderColorPicker(
            ctx.localization.ui("pick_pupil_color"),
            snapshot.zoomStateL(AppearanceState.pupilColor),
            df.pupilColors
          ),
          renderColorPicker(
            ctx.localization.ui("pick_guild_color1"),
            snapshot.zoomStateL(AppearanceState.guildColor1),
            builder.util.common.guildBannerColors
          ),
          renderColorPicker(
            ctx.localization.ui("pick_guild_color2"),
            snapshot.zoomStateL(AppearanceState.guildColor2),
            builder.util.common.guildBannerColors
          ),
          renderPicker(
            ctx.localization.ui("hairstyle"),
            builder.util.common.getHairStyleCount(cls),
            snapshot.zoomStateL(AppearanceState.hairIdx)
          ),
          renderPicker(
            ctx.localization.ui("body"),
            builder.util.common.getDressStyleCount(cls, appearance.sex),
            snapshot.zoomStateL(AppearanceState.bodyIdx)
          )
        )
      case None => EmptyVdom
    }
  }.build

  def renderColorPicker(label: VdomNode, state: StateSnapshot[Color], values: Seq[Color]): VdomElement =
    <.div(Theme.desktopBarItem,
      <.div(label),
      <.div(ColorPicker(state, values.flatMap(builder.util.common.generateCharacterColorShades))
      )
    )

  def renderPicker(label: VdomNode, cnt: Int, state: StateSnapshot[Int]): VdomElement =
    <.div(Theme.desktopBarItem,
      <.div(label),
      <.div(
        <.select(
          ^.value := state.value.toString,
          ^.onChange ==> { ev =>
          state.setState(ev.asInstanceOf[SyntheticMouseEvent[HTMLSelectElement]].target.value.toInt)
        })(0 until cnt map { i => <.option(^.value := i.toString, i) } toVdomArray)
      )
    )

  def renderSexSelect(ctx: Context, state: StateSnapshot[CharacterSex]): VdomElement =
    <.div(Theme.desktopBarItem,
      <.div(ctx.localization.ui("sex")),
      <.div(
        <.select(
          ^.value := state.value.value.toString,
          ^.onChange ==> { ev =>
          state.setState(CharacterSex.withValue(ev.asInstanceOf[SyntheticMouseEvent[HTMLSelectElement]].target.value.toInt))
        })(CharacterSex.values.map { i => <.option(^.value := i.value.toString, ctx.localization.sex(i.entryName)) } toVdomArray)
      )
    )
}
