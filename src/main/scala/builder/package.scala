import org.scalajs.dom.window
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

import scala.scalajs.js

package object builder {
  val CssSettings: Exports with Settings = scalacss.ProdDefaults

  def parseParams(): Params = {
    val map = window.location.search.substring(1).split("&").map { str =>
      val (k, v) = str.span(_ != '=')
      k -> js.URIUtils.decodeURIComponent(v.substring(1))
    }.toMap
    Params(map.getOrElse("lang", "en"), map.get("code"))
  }
}
