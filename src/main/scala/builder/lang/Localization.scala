package builder.lang

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import org.bitbucket.wakfuthesaurus.shared.data.Element

class Localization(translation: Translation) {
  private val component = ScalaComponent.builder[String]("Localization")
    .render_P { key => translation.getByKey(key) }
    .configure(Reusability.shouldComponentUpdate)
    .build

  final def characterClass(key: String): VdomElement =
    component(translation.characterClass(key))

  final def characteristic(key: String): VdomElement =
    component(translation.characteristic(key))

  final def characteristicDescription(key: String): VdomElement =
    component(translation.characteristicDescription(key))

  final def skill(key: String): VdomElement =
    component(translation.skill(key))

  final def itemRarity(key: String): VdomElement =
    component(translation.itemRarity(key))

  final def itemType(key: String): VdomElement =
    component(translation.itemType(key))

  final def itemRune(key: String): VdomElement =
    component(translation.itemRune(key))

  final def sex(key: String): VdomElement =
    component(translation.sex(key))

  final def element(element: Element): VdomElement =
    component(translation.element(element.entryName))

  final def ui(key: String): VdomElement =
    component(translation.ui(key))
}
