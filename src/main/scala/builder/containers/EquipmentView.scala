package builder.containers

import builder._
import builder.raw.onsen.Navigator.NavHandler
import builder.raw.onsen._
import builder.state._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import monocle.Lens

object EquipmentView {
  final case class Props(context: Context)

  def apply(key: Int, context: Context): VdomElement =
    component.withKey(key)(Props(context))

  sealed abstract class Route(val translationKey: String)
  object Route {
    case object Browse extends Route("gear")
    case object Search extends Route("search")
    final case class Item(lens: Lens[Equipment[SlotState], Option[SlotState]]) extends Route("item")
  }

  def renderToolbar(ctx: Context, title: VdomNode, nav: NavHandler[Route]): () => VdomElement = () =>
    Toolbar(modifier = "transparent")(
      <.div(^.className := "left")(BackButton(onClick = () => Callback(nav.popPage()))),
      <.div(^.className := "center", title)
    )

  def renderPage(ctx: Context): PartialFunction[(Route, NavHandler[Route]), VdomElement] = {
    case (Route.Browse, nav) =>
      EquipmentBrowserPage(ctx, nav)
    case (route@Route.Search, nav) =>
      Page(renderToolbar = renderToolbar(ctx, ctx.localization.ui(route.translationKey), nav))(
        EquipmentSearchForm(ctx, Callback(nav.popPage()))
      )
    case (Route.Item(lens), nav) =>
      ItemPage(ctx, lens, Callback(nav.popPage()))
  }

  private val component = ScalaFnComponent[Props] { case Props(ctx) =>
    Navigator[Route](
      swipeable = false,
      renderPage = renderPage(ctx),
      initialRoute = Route.Browse
    )
  }
}
