package builder.util

import builder.util
import builder.util.EffectProcessor.Modifiers
import org.bitbucket.wakfuthesaurus.shared.data.{ActionType => AT, _}

import scala.annotation.tailrec
import scala.util.matching.Regex

object EffectRenderer {
  def render(
    effects: Seq[Effect],
    level: Int,
    modifiers: Option[Modifiers] = None
  ): Element = Elements {
    effects.toIterator
      .filter(isEffectValid(level))
      .map(effectToElement(_)(modifiers, level))
      .collect(onlyValidElements)
      .toList
  }

  def render(
    effect: Effect,
    level: Int
  ): Element =
    if (isEffectValid(level)(effect))
      effectToElement(effect)(None, level)
    else NilElement

  private[this] def isEffectVisible(e: Effect): Boolean = e match {
    case s: SingleEffect => s.displayInSpellDescription
    case _               => true
  }

  private[this] def isEffectValid(lvl: Int)(e: Effect): Boolean = {
    lvl >= e.containerMinLevel &&
      lvl <= e.containerMaxLevel &&
      isEffectVisible(e)
  }

  private[this] def doesElementHaveText(c: Element): Boolean = c match {
    case Elements(es)                            => es.exists(doesElementHaveText)
    case LineElement(lc, _)                      => doesElementHaveText(lc)
    case TextElement(text) if text.trim.nonEmpty => true
    case _: StateLinkElement                     => true
    case _                                       => false
  }

  private[this] def onlyValidElements: PartialFunction[Element, Element] = {
    case Elements(es) if es.collectFirst(onlyValidElements).isDefined =>
      val validElems = es.collect(onlyValidElements)
      if (validElems.length == 1) validElems.head
      else Elements(validElems)
    case LineElement(Elements(es), pad)
      if es.exists(doesElementHaveText) && es
        .collectFirst(onlyValidElements)
        .isDefined =>
      LineElement(Elements(es.collect(onlyValidElements)), pad)
    case lc @ LineElement(txt: TextElement, _) if doesElementHaveText(txt) =>
      lc
    case c: IconElement                         => c
    case c @ TextElement(text) if text.nonEmpty => c
    case l: StateLinkElement                    => l
  }

  private[this] def formatParameter(
    idx: Int,
    effect: Effect,
    followedBy: CharSequence)(
    modifiers: Option[Modifiers],
    level: Int
  ): String = {

    @tailrec
    def formatParameter(param: TemplateParameter,
      effect: Effect,
      parent: Option[EffectGroup] = None): String =
      param match {
        case NumberParameter(base, inc, abs, perc, dec) =>
          val value = base + inc * level + 0.00001
          val processed =
            modifiers match {
              case Some(mod) =>
                EffectProcessor
                  .process(mod)(effect, idx)(value)
              case _ => value
            }
          val floored =
            if (dec > 0) {
              val div = math.pow(10, dec)
              math.floor(processed * div) / div
            } else math.floor(processed)
          val result =
            if (abs) math.abs(floored)
            else floored
          if (followedBy.toString.startsWith("%"))
            result.toString
          else if (perc) result.toString + "%"
          else result.toString
        case StateLink(name, refId, maxLevel) =>
          def link(lvl: Int) = s"[st:$name:$refId:$lvl]".toString

          if ((effect.action == AT.StateApply.id || effect.action == AT.ApplyStatePercentFunctionAreaHp.id ||
            effect.action == AT.ApplyDeathtag.id || effect.action == AT.ApplyStateForFecaArmor.id) &&
            maxLevel > 0 && !followedBy.toString.startsWith(" (Lvl.")) {
            val plus = math.floor(
              effect
                .originalParams(2) + effect.originalParams(3) * level + 0.00001)
            if (plus != 0)
              s"${link(plus.toInt)} (+$plus Lvl.)"
            else link(1)
          } else link(1)
        case EntityParameter(tpe, text, refId) =>
          EntityType.withValue(tpe) match {
            case EntityType.Aoe =>
              AreaOfEffect
                .withValueOpt(refId)
                .collect {
                  case AreaOfEffect.Point     => "CIRCLE"
                  case AreaOfEffect.Circle    => "CIRCLE"
                  case AreaOfEffect.Cross     => "CROSS"
                  case AreaOfEffect.T         => "CIRCLERING"
                  case AreaOfEffect.Ring      => "CIRCLERING"
                  case AreaOfEffect.Rectangle => "RECTANGLE"
                }
                .map("[" + _ + "]")
                .getOrElse("")
            case _ =>
              text.getOrElse("")
          }
        case InsertDescription =>
          effect match {
            case g: EffectGroup
              if effect.template.trim.isEmpty &&
                g.children.exists(isEffectValid(level)) =>
              g.children.toIterator
                .filter(isEffectValid(level))
                .map(format(_)(modifiers, level))
                .mkString("\n")
            case _ =>
              format(effect)(modifiers, level)
          }
        case InsertStateDescription(st, false) =>
          val stateLevel = (effect.originalParams(2) + effect
            .originalParams(3) * level + 0.00001).toInt
          st.effects.toIterator
            .filter(isEffectValid(stateLevel))
            .map(format(_)(modifiers, stateLevel))
            .mkString("\n")
        case InsertStateDescription(st, true) =>
          val stateLevel = (effect.originalParams(2) + effect
            .originalParams(3) * level + 0.00001).toInt
          st.effects.toIterator
            .filter(isEffectValid(stateLevel))
            .map("[pl]" + format(_)(modifiers, stateLevel))
            .mkString("\n")
        case ListChildren(false) =>
          effect match {
            case g: EffectGroup =>
              g.children.toIterator
                .filter(isEffectValid(level))
                .map(format(_)(modifiers, level))
                .mkString("\n")
            case e: SingleEffect =>
              format(e)(modifiers, level)
          }
        case ListChildren(true) =>
          effect match {
            case g: EffectGroup =>
              g.children.toIterator
                .filter(isEffectValid(level))
                .map("[pl]" + format(_)(modifiers, level))
                .mkString("\n")
            case e: SingleEffect =>
              format(e)(modifiers, level)
          }
        case ChildParameter(cidx, Some(p)) =>
          effect match {
            case g: EffectGroup if g.children.isDefinedAt(cidx) =>
              formatParameter(p, g.children(cidx), Some(g))
            case _ =>
              parent match {
                case Some(pt) if pt.children.isDefinedAt(cidx) =>
                  formatParameter(p, pt.children(cidx), parent)
                case None =>
                  formatParameter(p, effect, parent)
              }
          }
        case _ => ""
      }

    formatParameter(effect.params(idx), effect)
  }

  private[this] def getAoeIcon(effect: Effect): Option[String] = {
    val params = effect.areaSize
    AreaOfEffect.withValueOpt(effect.areaShape).flatMap {
      case AreaOfEffect.Point => None
      case AreaOfEffect.Circle =>
        if (params(0) == 0) None
        else Some("CIRCLE")
      case AreaOfEffect.Cross =>
        val upSize = params(0)
        val (leftSize, downSize, rightSize) =
          if (params.length == 2)
            (params(1), upSize, params(1))
          else if (params.length == 4)
            (params(2), params(1), params(3))
          else (upSize, upSize, upSize)
        if (downSize == 0 && upSize == 0 && leftSize == 0 && rightSize == 0)
          None
        else if (downSize == 0 && upSize == 0)
          Some("VLINE")
        else if (leftSize == 0 && rightSize == 0)
          Some("HLINE")
        else Some("CROSS")
      case AreaOfEffect.T | AreaOfEffect.TI =>
        val barmidLength = params(0)
        val footLength = params(1)
        if (barmidLength == 0 && footLength == 0)
          None
        else if (barmidLength == 0)
          Some("VLINE")
        else if (footLength == 0)
          Some("HLINE")
        else Some("CIRCLERING")
      case AreaOfEffect.Ring =>
        val innerRadius = math.min(params(0), params(1))
        val outerRadius = math.max(params(0), params(1))
        if (innerRadius == 0 && outerRadius == 0) None
        else if (innerRadius == 0)
          Some("CIRCLE")
        else Some("CIRCLERING")
      case AreaOfEffect.Rectangle =>
        val width = params(0)
        val height =
          if (params.length > 1) params(1)
          else width
        if (height == 0 && width == 0) None
        else if (height == width)
          Some("SQUARE")
        else Some("RECTANGLE")
      case AreaOfEffect.RectRing =>
        val (innerWidth, innerHeight, outerWidth, outerHeight) =
          if (params.length == 2)
            (math.min(params(0), params(1)),
              math.min(params(0), params(1)),
              math.max(params(0), params(1)),
              math.max(params(0), params(1)))
          else
            (params(0), params(1), params(2), params(3))
        if (outerHeight == 0 && outerWidth == 0) None
        else if (outerHeight == outerWidth && innerHeight == innerWidth)
          Some("SQUARERING")
        else Some("RECTANGLERING")
      case AreaOfEffect.FreePointsForm => None
      case AreaOfEffect.DirectedRectangle =>
        val halfWidth = params(0)
        val length = params(1)
        if (length == 1) {
          if (halfWidth == 0) None
          else Some("HLINE")
        } else {
          if (halfWidth == 0) Some("VLINE")
          else Some("RECTANGLE")
        }
      case _ => None
    }
  }

  private[this] def format(effect: Effect)(modifiers: Option[Modifiers],
    level: Int): String = {
    val formatLevel = (_: String).replace(LevelPattern, level.toString)

    val formatArithmetic = ArithmeticPattern.replaceAllIn(
      _: String, { m ⇒
        import scala.scalajs.js
        import js.Dynamic.{global => g}

        val decimals =
          if (m.group(1) == null) 0
          else m.group(1).substring(0, m.group(1).length - 1).toInt
        val expr = m.group(2) + m.group(4)
        val pw = math.pow(10, decimals)
        val res =
          try {
            g.eval(expr.replace("%", "")).asInstanceOf[Double]
          } catch {
            case _: Throwable => 0
          }
        val result = math.floor(res * pw) / pw
        result.toString
      }
    )

    val formatArgs = ArgumentPattern.replaceAllIn(
      _: String, { m ⇒
        val idx = m.group(1).toInt
        if (effect.action == AT.ElementSpellGain.id && idx == 1) {
          "[el" + math.floor(effect.originalParams(0) + 0.00001).toString + "]"
        } else
          formatParameter(idx, effect, followedBy = m.after)(modifiers, level)
      }
    )

    val replaceLt = (_: String).replace("&lt;", "<")
    val replaceGt = (_: String).replace("&gt;", ">")
    val removeTags = (_: String).replaceAll("<[^>]*>", "")

    val template =
      AT.byId(effect.action)
        .collect {
          case AT.HpFireLoss | AT.RandomHpFireLoss | AT.HpGainFire |
               AT.FireResistGain | AT.FireMasteryGain =>
            effect.template + "[el1]"
          case AT.HpEarthLoss | AT.RandomHpEarthLoss | AT.HpGainEarth |
               AT.EarthResistGain | AT.EarthMasteryGain =>
            effect.template + "[el3]"
          case AT.HpWaterLoss | AT.RandomHpWaterLoss | AT.HpGainWater |
               AT.WaterResistGain | AT.WaterMasteryGain =>
            effect.template + "[el2]"
          case AT.HpAirLoss | AT.RandomHpAirLoss | AT.HpGainAir |
               AT.AirResistGain | AT.AirMasteryGain =>
            effect.template + "[el4]"
          case AT.HpStasisLoss                 => effect.template + "[el5]"
          case AT.HpLightLoss | AT.HpGainLight => effect.template + "[el6]"
        }
        .map(_ + getAoeIcon(effect).map('[' + _ + ']').getOrElse(""))
        .getOrElse(effect.template)

    formatArithmetic compose
      formatArgs compose
      formatLevel compose
      replaceLt compose
      replaceGt compose
      removeTags compose
      (util.common.formatConditions(_: String, effect, level)) apply template
  }

  private[this] def effectToElement(
    effect: Effect)(modifiers: Option[Modifiers], level: Int): Element = {
    if (effect.id == 98204 || effect.id == 98832 || effect.id == 159189 || effect.id == 145437 || effect.id == 145700)
      NilElement
    else
      effect match {
        case g: EffectGroup
          if effect.displayInSpellDescription &&
            effect.template.trim.isEmpty &&
            g.children.exists(isEffectValid(level)) =>
          Elements {
            g.children.toIterator
              .filter(isEffectValid(level))
              .map(e => LineElement(effectToElement(e)(modifiers, level)))
              .toList
          }
        case _ =>
          val formatted = format(effect)(modifiers, level)
          val rows = formatted
            .split("\n")
            .toIterator
            .map { row =>
              var end = 0
              val exprs =
                GeneralPattern
                  .findAllMatchIn(row)
                  .toList
                  .map { m =>
                    val prefix = m.before.toString.substring(end)
                    end = m.end
                    (prefix, m)
                  }
              (row.substring(end), exprs)
            }

          Elements {
            rows.map {
              case (postfix, row) =>
                var padLine = false

                val elems =
                  row.map {
                    case (prefix, m) =>
                      val pre = TextElement(prefix)
                      if (m.group(1) == "pl") {
                        padLine = true
                        Elements(
                          pre :: IconElement("icons/format/plot.png") :: Nil)
                      } else if (m.group(1).startsWith("el")) {
                        val v = m.group(1).substring(2)
                        if (v.nonEmpty) {
                          val elem = Element.withValue(v.toInt)
                          Elements(
                            pre :: IconElement(
                              s"icons/elements/small_${elem.entryName}.png") :: Nil)
                        } else pre
                      } else if (m.group(1).startsWith("st:")) {
                        val parts = m.group(1).substring(3).split(':')
                        val name = parts(0)
                        val idx = parts(1).toInt
                        val lvl = parts(2).toInt
                        Elements(pre :: StateLinkElement(name, idx, lvl) :: Nil)
                      } else if (m.group(1) == "ae") {
                        getAoeIcon(effect) match {
                          case Some(icon) =>
                            Elements(
                              pre :: IconElement(s"icons/aoe/$icon.png") :: Nil)
                          case None => pre
                        }
                      } else if (m.group(1) != null && FormatIconSet.contains(
                        m.group(1))) {
                        val v = m.group(1)
                        Elements(
                          pre :: IconElement(s"icons/format/$v.png") :: Nil)
                      } else if (m.group(1) != null && FormatAoeSet.contains(
                        m.group(1))) {
                        val v = m.group(1)
                        Elements(pre :: IconElement(s"icons/aoe/$v.png") :: Nil)
                      } else Elements(pre :: TextElement(m.group(0)) :: Nil)
                  }
                LineElement(Elements(elems ::: TextElement(postfix) :: Nil),
                  padLine)
            }.toList
          }
      }
  }

  sealed trait Element

  final case class IconElement(path: String) extends Element

  final case class TextElement(text: String) extends Element

  final case class Elements(elems: List[Element]) extends Element

  final case class LineElement(elem: Element, pad: Boolean = false)
    extends Element

  final case class StateLinkElement(name: String, refId: Int, lvl: Int)
    extends Element

  case object NilElement extends Element

  val GeneralPattern: Regex = """\[(.*?)\]""".r
  val ArithmeticPattern: Regex =
    """\|([0-9]d)?([0-9]+([,\.][0-9]+)?)([^|]*)\|""".r
  val ArgumentPattern: Regex = """\`([0-9]+)\`""".r
  val LevelPattern: String = "[@lv]"
  val FormatIconSet: Set[String] = Set(
    "ally",
    "backstab",
    "barrel",
    "bomb",
    "caster",
    "chromatic",
    "deposit",
    "drake",
    "enemy",
    "exalte",
    "fighter",
    "glyph",
    "invisible",
    "lucky",
    "natural",
    "paw",
    "plot",
    "portal",
    "puppet",
    "seed",
    "serein",
    "shield",
    "sidestab",
    "taque",
    "tique",
    "totem",
    "undrake",
    "unnatural",
    "rune1",
    "rune2",
    "rune3",
    "rune4"
  )

  val FormatAoeSet: Set[String] = Set(
    "CIRCLE",
    "CIRCLERING",
    "CONE",
    "CROSS",
    "CROSSDIAGONAL",
    "HLINE",
    "POINT",
    "RECTANGLE",
    "RECTANGLERING",
    "RING",
    "SPECIAL",
    "SQUARE",
    "SQUARERING",
    "T",
    "TREVERSED",
    "VLINE"
  )
}
