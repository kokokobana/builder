package builder.containers

import builder._
import builder.components.FairUse
import builder.raw.onsen._
import builder.state.{BuilderState, SideMenuState, ViewState}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import org.bitbucket.wakfuthesaurus.shared.data.CharacterClass
import scalacss.ScalaCssReact._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

object BuilderContainer {
  final case class Props(context: Context, model: ModelProxy[_])

  def apply(context: Context): VdomElement =
    MainCircuit.wrap(identity[BuilderState] _) { model =>
      component(Props(context, model))
    }

  def initialize($: ComponentDidMount[Props, Unit, Unit]): Callback = Callback.lazily {
    MainCircuit.params.build match {
      case Some(code) =>
        $.props.model.dispatchCB(LoadBuild(code))
      case None =>
        $.props.model.dispatchCB(LoadClassData(CharacterClass.Feca))
    }
  }

  private val component = ScalaComponent.builder[Props]("BuilderContainer")
    .render_P { case Props(ctx, _) => VdomArray(
      if (!util.common.isTouchDevice) DesktopBar(ctx) else EmptyVdom,
      <.div(Theme.component, (^.className := "desktop-mode").when(!util.common.isTouchDevice),
        <.div(Theme.characterPreview, CharacterCanvas()).when(!util.common.isTouchDevice),
        if (util.common.isTouchDevice) {
          Splitter.apply(
            SideMenu(ctx),
            SplitterContent.apply(Builder(ctx))
          )
        } else Builder(ctx),
        <.div(Theme.fairUseContainer, FairUse()).when(!util.common.isTouchDevice)
      )
    )
    }.componentDidMount(initialize)
    .build
}

object Builder {
  final case class Props(context: Context, model: ModelProxy[ViewState])

  def apply(context: Context): VdomElement =
    MainCircuit.connect(_.view).apply { model =>
      component(Props(context, model))
    }

  def renderToolbar(ctx: Context, openSideMenu: Callback): () => VdomElement = () =>
    Toolbar()(
      <.div(^.className := "center", ctx.localization.ui("builder")),
      <.div(^.className := "right")(
        ToolbarButton(onClick = () => openSideMenu)(
          Icon(icon = "ion-navicon, material:md-menu")
        )
      )
    )

  def characteristicTab(ctx: Context)(i: Int): Tabbar.Entry =
    Tabbar.Entry(
      Page()(^.key := i)(CharacteristicView(ctx)).rawNode,
      Tab(label = ctx.translation.ui("stats"), icon = "md-equalizer")(^.key := i).rawNode
    )
  def skillsTab(ctx: Context)(i: Int): Tabbar.Entry =
    Tabbar.Entry(
      Page()(^.key := i)(SkillsView(ctx)).rawNode,
      Tab(label = ctx.translation.ui("skills"), icon = "md-tune")(^.key := i).rawNode
    )
  def spellsTab(ctx: Context)(i: Int): Tabbar.Entry =
    Tabbar.Entry(
      SpellsView(i, ctx).rawNode,
      Tab(label = ctx.translation.ui("spells"), icon = "md-book")(^.key := i).rawNode
    )
  def equipmentTab(ctx: Context)(i: Int): Tabbar.Entry =
    Tabbar.Entry(
      EquipmentView(i, ctx).rawNode,
      Tab(label = ctx.translation.ui("gear"), icon = "md-shield-security")(^.key := i).rawNode
    )
  def customTab(ctx: Context)(i: Int): Tabbar.Entry =
    Tabbar.Entry(
      Page()(^.key := i)(CharacteristicCustomization(ctx)).rawNode,
      Tab(label = ctx.translation.ui("custom"), icon = "md-edit")(^.key := i).rawNode
    )

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    Page(
      renderToolbar =
        if (util.common.isTouchDevice)
          renderToolbar(ctx, model.dispatchCB(SetSideMenu(open = true, silent = false)))
        else js.undefined,
      renderModal = () => Loading.apply
    )(
      Tabbar(
        swipeable = util.common.isTouchDevice,
        position = if (util.common.isTouchDevice) "auto" else "top",
        animation = if (util.common.isTouchDevice) js.undefined else js.defined("none"),
        index = model.value.activeIndex,
        onPreChange = i => model.dispatchCB(SetView(i.index)),
        renderTabs = () =>
          Seq(
            characteristicTab(ctx) _,
            skillsTab(ctx) _,
            spellsTab(ctx) _,
            equipmentTab(ctx) _,
            customTab(ctx) _
          ).zipWithIndex
            .map(Function.tupled(_ apply _))
            .toJSArray
      ),
      BuildCodeDialog(ctx)
    )
  }
}

object DesktopBar {
  final case class Props(context: Context)

  def apply(ctx: Context): VdomElement = component(Props(ctx))

  private val component = ScalaFnComponent[Props] { case Props(ctx) =>
    <.div(Theme.desktopBar,
      ^.className := "desktop-bar",
      renderClassSelect(ctx),
      renderLevelInput(ctx),
      <.div(Theme.elementPriorityListItem,
        ElementPriorityList(ctx)
      ),
      CharacterPreviewSettings(ctx),
      ShareButton(ctx)
    )
  }

  def renderClassSelect(ctx: Context): VdomElement =
    <.div(Theme.classSelectItem,
      <.div(ctx.localization.ui("class")),
      <.div(ClassSelect(ctx))
    )

  def renderLevelInput(ctx: Context): VdomElement =
    <.div(Theme.levelInputItem,
      <.div(ctx.localization.ui("level_short")),
      <.div(LevelInput(ctx)),
    )
}

object ShareButton {
  final case class Props(ctx: Context, model: ModelProxy[_])

  def apply(ctx: Context): VdomElement =
    MainCircuit.wrap(m => m)(m => component(Props(ctx, m)))

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    Button(onClick = () => model.dispatchCB(ShareBuild))(
      ctx.localization.ui("share"),
      Icon(icon = "share", style = js.Dynamic.literal("marginLeft" -> 4))
    )
  }
}

object SideMenu {
  final case class Props(context: Context, model: ModelProxy[SideMenuState])

  def apply(context: Context): VdomElement =
    MainCircuit.connect(_.sideMenu).apply { model =>
      component(Props(context, model))
    }

  private val component = ScalaFnComponent[Props] { case Props(ctx, model) =>
    SplitterSide(
      side = "left",
      width = 200,
      collapse = true,
      swipeable = util.common.isTouchDevice,
      isOpen = model.value.open,
      onClose = () => model.dispatchCB(SetSideMenu(open = false, silent = true)),
      onOpen = () => model.dispatchCB(SetSideMenu(open = true, silent = true))
    )(
      Page()(
        <.div(Theme.splitterContent,
          <.div(Theme.splitterRow,
            <.span(Theme.splitterLabel, ctx.localization.ui("class")),
            ClassSelect(ctx)
          ),
          <.div(Theme.splitterRow,
            <.span(Theme.splitterLabel, ctx.localization.ui("level_short")),
            <.div(Theme.levelInput, LevelInput(ctx))
          ),
          <.div(Theme.splitterRow,
            <.span(Theme.splitterLabel, ctx.localization.ui("element_priority")),
            ElementPriorityList(ctx)
          ),
          <.div(Theme.splitterButton, ShareButton(ctx)),
          FairUse()
        )
      )
    )
  }
}
