package builder.raw

import com.payalabs.scalajs.react.bridge.{ReactBridgeComponent, WithPropsAndTagsMods}
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.vdom.VdomElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object InfiniteScroll extends ReactBridgeComponent {
  @js.native
  @JSImport("react-infinite-scroller", JSImport.Namespace)
  private object Raw extends js.Object

  override protected lazy val componentValue: js.Any = Raw
  def apply(pageStart: Int, loadMore: Callback, hasMore: Boolean, loader: VdomElement, useWindow: Boolean): WithPropsAndTagsMods = autoNoTagMods
}
