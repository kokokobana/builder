package builder.raw

import com.payalabs.scalajs.react.bridge.ReactBridgeComponent
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.raw.React
import japgolly.scalajs.react.vdom.VdomElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object ReactResponsiveSelect extends ReactBridgeComponent {
  @js.native
  @JSImport("react-responsive-select", JSImport.Default)
  private object Raw extends js.Object

  @js.native
  trait MultiChange extends js.Object {
    val altered: Boolean
    val options: js.Array[Option]
  }

  @js.native
  trait Option extends js.Object {
    val altered: Boolean
    var text: String
    var value: String
    var markup: React.Node
  }

  object Option {
    def apply(text: String, value: String, markup: React.Node): Option = {
      val x = new js.Object().asInstanceOf[Option]
      x.text = text
      x.value = value
      x.markup = markup
      x
    }
  }

  override protected lazy val componentValue: js.Any = Raw

  def apply[T](
    name: js.UndefOr[String] = js.undefined,
    multiselect: js.UndefOr[Boolean] = js.undefined,
    options: js.Array[Option],
    selectedValues: js.UndefOr[js.Array[String]] = js.undefined,
    onChange: js.UndefOr[T => Callback] = js.undefined,
    caretIcon: js.UndefOr[VdomElement] = js.undefined,
    customLabelRenderer: js.UndefOr[js.Function1[MultiChange, React.Node]] = js.undefined
  ): VdomElement = autoNoTagModsNoChildren
}
