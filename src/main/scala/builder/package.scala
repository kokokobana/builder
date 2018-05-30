import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

package object builder {
  val CssSettings: Exports with Settings = scalacss.ProdDefaults
}
