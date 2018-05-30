package builder.raw
import com.payalabs.scalajs.react.bridge.{ReactBridgeComponent, WithProps, WithPropsAndTagsMods, WithPropsNoChildren}
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.raw._
import japgolly.scalajs.react.vdom.VdomElement

import scala.annotation.meta.field
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSImport}
import cats.implicits._
import org.scalajs.dom.raw.HTMLInputElement

import scala.scalajs.js.Promise

@js.native
@JSImport("onsenui", JSImport.Namespace)
object ons extends js.Object {
  @js.native
  object notification extends js.Object {
    def alert(message: String): Unit = js.native
  }

  def ready(fn: js.Function0[Unit]): Unit = js.native
}

object onsen {
  @js.native
  @JSImport("react-onsenui", JSImport.Namespace)
  private object Raw extends js.Object {
    val Page: js.Any = js.native
    val Button: js.Any = js.native
    val Toolbar: js.Any = js.native
    val BottomToolbar: js.Any = js.native
    val ToolbarButton: js.Any = js.native
    val Icon: js.Any = js.native
    val Splitter: js.Any = js.native
    val SplitterSide: js.Any = js.native
    val SplitterContent: js.Any = js.native
    val List: js.Any = js.native
    val ListItem: js.Any = js.native
    val Tabbar: js.Any = js.native
    val Tab: js.Any = js.native
    val Fab: js.Any = js.native
    val BackButton: js.Any = js.native
    val Navigator: js.Any = js.native
    val Input: js.Any = js.native
    val Checkbox: js.Any = js.native
    val Radio: js.Any = js.native
    val Range: js.Any = js.native
    val SearchInput: js.Any = js.native
    val Select: js.Any = js.native
    val Switch: js.Any = js.native
    val ProgressCircular: js.Any = js.native
    val Toast: js.Any = js.native
    val AlertDialog: js.Any = js.native
    val Modal: js.Any = js.native
    val Segment: js.Any = js.native
  }

  object Page extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Page
    def apply(
      renderToolbar: js.UndefOr[() => VdomElement] = js.undefined,
      renderBottomToolbar: js.UndefOr[() => VdomElement] = js.undefined,
      renderFixed: js.UndefOr[() => VdomElement] = js.undefined,
      renderModal: js.UndefOr[() => VdomElement] = js.undefined,
      onInfiniteScroll: js.UndefOr[js.Function1[js.Function0[Unit], Unit]] = js.undefined
    ): WithProps = auto
  }

  object Button extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Button
    def apply(
      onClick: js.UndefOr[() => Callback] = js.undefined,
      modifier: js.UndefOr[String] = js.undefined
    ): WithPropsAndTagsMods = autoNoTagMods
  }

  object Toolbar extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Toolbar
    def apply(modifier: js.UndefOr[String] = js.undefined): WithPropsAndTagsMods = autoNoTagMods
  }

  object BottomToolbar extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.BottomToolbar
    def apply(modifier: js.UndefOr[String] = js.undefined): WithPropsAndTagsMods = autoNoTagMods
  }

  object ToolbarButton extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.ToolbarButton
    def apply(onClick: js.UndefOr[() => Callback] = js.undefined): WithPropsAndTagsMods = autoNoTagMods
  }

  object Icon extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Icon

    def apply(
      icon: String,
      size: Int,
      color: String
    ): VdomElement =
      apply(icon, js.defined(js.Dynamic.literal("color" -> color, "fontSize" -> s"${size}px")))

    def apply(
      icon: String,
      size: Int,
    ): VdomElement =
      apply(icon, js.defined(js.Dynamic.literal("fontSize" -> s"${size}px")))

    def apply(
      icon: String,
      style: js.UndefOr[js.Object] = js.undefined
    ): VdomElement = autoNoTagModsNoChildren
  }

  object Splitter extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Splitter
    def apply: WithPropsAndTagsMods = autoNoTagMods
  }

  object SplitterSide extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.SplitterSide
    def apply(
      isOpen: Boolean,
      onClose: () => Callback,
      onOpen: () => Callback,
      side: js.UndefOr[String] = js.undefined,
      width: js.UndefOr[Int] = js.undefined,
      collapse: js.UndefOr[Boolean] = js.undefined,
      swipeable: js.UndefOr[Boolean] = js.undefined
    ): WithPropsAndTagsMods = autoNoTagMods
  }

  object SplitterContent extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.SplitterContent
    def apply: WithPropsAndTagsMods = autoNoTagMods
  }

  object List extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.List
    def apply(): WithProps = auto
  }

  object Tabbar extends ReactBridgeComponent {
    @js.native
    trait TabEvent extends js.Object {
      val index: Int
    }

    final case class Entry(
      @(JSExport @field) content: React.Node,
      @(JSExport @field) tab: React.Node
    )

    override protected lazy val componentValue: js.Any = Raw.Tabbar
    def apply(
      swipeable: Boolean,
      index: Int,
      onPreChange: TabEvent => Callback,
      renderTabs: () => js.Array[Entry],
      position: js.UndefOr[String] = js.undefined,
      id: js.UndefOr[String] = js.undefined,
      animation: js.UndefOr[String] = js.undefined
    ): VdomElement = autoNoTagModsNoChildren
  }

  object Tab extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Tab
    def apply(label: String, icon: String): WithPropsNoChildren = autoNoChildren
  }

  object ListItem extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.ListItem
    def apply(key: Int): WithPropsAndTagsMods = autoNoTagMods
  }

  object Fab extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Fab
    def apply(onClick: () => Callback, position: String): WithPropsAndTagsMods = autoNoTagMods
  }

  object BackButton extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.BackButton
    def apply(onClick: () => Callback): WithPropsAndTagsMods = autoNoTagMods
  }

  object Navigator extends ReactBridgeComponent {
    @js.native
    trait NavHandler[R] extends js.Object {
      def pushPage(route: R): Unit
      def popPage(): Promise[js.Any]
      def popPage(options: js.Any): Promise[js.Any]
    }

    override protected lazy val componentValue: js.Any = Raw.Navigator

    def apply[R](
      swipeable: Boolean,
      renderPage: ((R, NavHandler[R])) => VdomElement,
      initialRoute: R
    ): VdomElement = apply(swipeable, Function.untupled(renderPage.rmap(_.rawNode)), initialRoute.asInstanceOf[js.Any])

    def apply[R](
      swipeable: Boolean,
      renderPage: js.Function2[R, NavHandler[R], React.Node],
      initialRoute: js.Any
    ): VdomElement = autoNoTagModsNoChildren
  }

  object Input extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Input
    def apply(
      onChange: js.UndefOr[SyntheticKeyboardEvent[HTMLInputElement] => Callback] = js.undefined,
      value: js.UndefOr[String] = js.undefined,
      placeholder: js.UndefOr[String] = js.undefined,
      `type`: js.UndefOr[String] = js.undefined,
      modifier: js.UndefOr[String] = js.undefined,
      float: js.UndefOr[Boolean] = js.undefined,
      disabled: js.UndefOr[Boolean] = js.undefined,
      inputId: js.UndefOr[String] = js.undefined
    ): VdomElement = autoNoTagModsNoChildren
  }

  object SearchInput extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.SearchInput
    def apply(
      onChange: SyntheticKeyboardEvent[HTMLInputElement] => Callback,
      value: js.UndefOr[String] = js.undefined,
      placeholder: js.UndefOr[String] = js.undefined,
      modifier: js.UndefOr[String] = js.undefined
    ): VdomElement = autoNoTagModsNoChildren
  }

  object ProgressCircular extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.ProgressCircular
    def apply(
      value: js.UndefOr[Int] = js.undefined,
      indeterminate: js.UndefOr[Boolean] = js.undefined
    ): VdomElement = autoNoTagModsNoChildren
  }

  object Switch extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Switch
    def apply(
      checked: Boolean,
      onChange: (SyntheticMouseEvent[HTMLInputElement]) => Callback
    ): VdomElement = autoNoTagModsNoChildren
  }

  object Checkbox extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Checkbox
    def apply(
      checked: Boolean,
      onChange: (SyntheticMouseEvent[HTMLInputElement]) => Callback
    ): VdomElement = autoNoTagModsNoChildren
  }

  object Toast extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Toast
    def apply(isOpen: Boolean): WithPropsAndTagsMods = autoNoTagMods
  }

  object AlertDialog extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.AlertDialog
    def apply(
      isOpen: Boolean,
      isCancelable: Boolean
    ): WithPropsAndTagsMods = autoNoTagMods
  }

  object Modal extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Modal
    def apply(
      isOpen: Boolean
    ): WithPropsAndTagsMods = autoNoTagMods
  }

  object Select extends ReactBridgeComponent {
    override protected lazy val componentValue: js.Any = Raw.Select
    def apply(
      onChange: (SyntheticMouseEvent[HTMLInputElement]) => Callback,
      value: String
    ): WithPropsAndTagsMods = autoNoTagMods
  }

  object Segment extends ReactBridgeComponent {
    @js.native
    trait SegmentEvent extends js.Object {
      val index: Int
    }

    override protected lazy val componentValue: js.Any = Raw.Segment
    def apply(
      index: Int,
      onPostChange: SegmentEvent => Callback,
      tabbarId: js.UndefOr[String] = js.undefined
    ): WithPropsAndTagsMods = autoNoTagMods
  }
}
