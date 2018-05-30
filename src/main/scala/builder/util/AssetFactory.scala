package builder.util

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._

final class AssetFactory(s3URL: String) {
  def icon(path: String, styleMod: Option[TagMod] = None): VdomElement =
    Image.component(Image.Props(makeAssetIconLink(path), styleMod))
  def itemIcon(id: Int, styleMod: Option[TagMod] = None): VdomElement =
    Image.component(Image.Props(makeItemIconLink(id), styleMod))
  def spellIcon(id: Int, styleMod: Option[TagMod] = None): VdomElement =
    Image.component(Image.Props(makeSpellIconLink(id), styleMod))
  def stateIcon(id: String, styleMod: Option[TagMod] = None): VdomElement =
    Image.component(Image.Props(makeStateIconLink(id), styleMod))

  def makeItemIconLink(id: Int): String = s"$s3URL/icons/item/$id.png"
  def makeSpellIconLink(id: Int): String = s"$s3URL/icons/spell/$id.png"
  def makeStateIconLink(id: String): String = s"$s3URL/icons/state/$id.png"
  def makeAssetLink(path: String): String = s"$s3URL/content/$path"
  def makeAssetIconLink(path: String): String = s"$s3URL/content/icons/$path"
}

private object Image {
  final case class Props(src: String, styleMod: Option[TagMod])
  implicit val reusability: Reusability[Props] = Reusability.byRefOr_==

  val component = ScalaComponent.builder[Props]("Image")
    .render_P { props => <.img(^.src := props.src, props.styleMod.whenDefined) }
    .configure(Reusability.shouldComponentUpdate)
    .build
}
