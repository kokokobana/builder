package builder.util

import builder.util.EffectProcessor.Modifiers
import builder.util.EffectRenderer._
import builder.{Context, Theme}
import japgolly.scalajs.react.vdom.html_<^._
import org.bitbucket.wakfuthesaurus.shared.data.Effect
import scalacss.ScalaCssReact._

object EffectList {
  def renderList(
    context: Context
  )(effects: Seq[Effect],
    level: Int,
    modifiers: Option[Modifiers] = None,
    mod: Option[TagMod] = None
  ): VdomNode =
    EffectRenderer.render(effects, level, modifiers) match {
      case Elements(Nil) => EmptyVdom
      case e =>
        <.div(mod.whenDefined, renderToDom(context)(e))
    }

  def renderEntry(
    context: Context
  )(effect: Effect,
    level: Int,
    key: Option[Int] = None
  ): VdomNode = renderToDom(context)(EffectRenderer.render(effect, level), key)

  private def renderToDom(context: Context)(element: Element, key: Option[Int] = None): VdomNode =
    element match {
      case IconElement(path) =>
        <.img(Theme.iconWithinEffect,
          ^.key :=? key,
          ^.src := context.assets.makeAssetLink(path))
      case StateLinkElement(name, _, _) => name + " "
      case TextElement(text) =>
        val trimmed = text.trim
          if (trimmed.nonEmpty && trimmed.charAt(0) == ':')
            trimmed.substring(1) + " "
          else trimmed + " "
      case Elements(elem :: Nil) =>
        renderToDom(context)(elem)
      case Elements(elems) =>
        elems.zipWithIndex.map {
          case (e, i) =>
            renderToDom(context)(e, Some(i))
        }.toVdomArray
      case LineElement(e, true) =>
          <.div(^.key :=? key, renderToDom(context)(e))
      case LineElement(e, false) =>
          <.div(^.key :=? key, renderToDom(context)(e))
      case NilElement => EmptyVdom
    }
}
