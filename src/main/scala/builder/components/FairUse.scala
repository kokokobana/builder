package builder.components

import builder.Theme
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.ScalaCssReact._

object FairUse {
  def apply(): VdomElement =
    <.div(Theme.fairUse,
      "This is an unofficial site with no ties to Ankama. ",
      "Wakfu the game is published by Ankama Games. ",
      "The use of any resources distributed as a part of the public build of the game is done under the Fair Use doctrine. ",
      "This app is nonprofit software available open source on ", <.a(^.href := "https://github.com/kokokobana/builder", "Github"), ".")
}
