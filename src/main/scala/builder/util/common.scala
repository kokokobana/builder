package builder.util

import org.bitbucket.wakfuthesaurus.shared.data._
import org.scalajs.dom.html.Image
import org.scalajs.dom.{Event, document}

import scala.annotation.tailrec
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.util.matching.Regex

object common {
  val isTouchDevice: Boolean =
    js.eval("!!(window.navigator.maxTouchPoints || 'ontouchstart' in document.documentElement)").asInstanceOf[Boolean]

  def loadImage(url: String): Future[Image] = {
    val promise = Promise[Image]()
    val image = document.createElement("img").asInstanceOf[Image]
    image.asInstanceOf[js.Dynamic].crossOrigin = "Anonymous"
    image.src = url
    image.onload = (_: Event) ⇒ promise.success(image)
    promise.future
  }

  private[this] val conditionPattern: Regex =
    """\{([0-9]+)\s*\?([^\{\}]*):([^\{\}]*)\}""".r

  @tailrec
  def formatConditions(template: String, effect: Effect, level: Int): String = {
    conditionPattern.findFirstMatchIn(template) match {
      case Some(m) ⇒
        val idx = m.group(1).toInt
        val out = effect.params(idx) match {
          case ConditionParameter(const, arg, op) ⇒
            val argNum = arg.collect {
              case NumberParameter(base, inc, _, _, _) ⇒
                math.floor(base + inc * level + 0.00001)
            }
            val argStr = arg.collect {
              case EntityParameter(_, Some(txt), _) ⇒ txt
            }
            val result =
              op match {
                case "<" ⇒ argNum.exists(_ < const)
                case ">" ⇒ argNum.exists(_ > const)
                case "=" ⇒ argNum.contains(const.toDouble)
                case "~" ⇒
                  argNum.exists(_ != 0) ||
                    argStr.exists(_.length > 0)
                case "!" ⇒
                  argNum.contains(0) ||
                    argStr.exists(_.length == 0) ||
                    argStr.isEmpty
                case "+" ⇒ argNum.exists(_ > 0)
                case "-" ⇒ argNum.exists(_ < 0)
                case "*" ⇒ const == 0
                case _ => false
              }
            m.before +
              (if (result) m.group(2) else m.group(3)) +
              m.after
          case _ ⇒
            m.before + m.after.toString
        }
        formatConditions(out, effect, level)
      case None ⇒
        template
    }
  }

  val guildBannerColors: Seq[Color] = {
    val colors: Seq[Float] = Seq(255, 255, 255, 106, 106, 106, 255, 128, 128,
      255, 0, 0, 255, 128, 192, 255, 0, 128, 255, 128, 255, 255, 0, 255, 128,
      0, 255, 192, 128, 255, 128, 128, 255, 0, 0, 255, 0, 128, 255, 128, 192,
      255, 128, 255, 255, 0, 255, 255, 0, 255, 128, 128, 255, 192, 128, 255,
      128, 0, 255, 0, 128, 255, 0, 192, 255, 128, 255, 255, 128, 255, 255, 0,
      255, 128, 0, 255, 192, 128)
      .map(_ / 255.999f)

    colors.grouped(3).toSeq.flatMap {
      case Seq(r, g, b) =>
        def withIntensity(intensity: Float): Color = {
          val red = Math.min(1.0f, r * intensity)
          val green = Math.min(1.0f, g * intensity)
          val blue = Math.min(1.0f, b * intensity)
          Color(red, green, blue, 1.0f)
        }

        Seq(
          withIntensity(1.0f),
          withIntensity(0.8f),
          withIntensity(0.6f),
          withIntensity(0.4f))
    }
  }

  def generateCharacterColorShades(color: Color): Seq[Color] = {
    def withShade(x: Float): Color =
      Color(
        red = color.red * (1 - x),
        green = color.green * (1 - x),
        blue = color.blue * (1 - x),
        alpha = color.alpha
      )

    def withTint(x: Float): Color =
      Color(
        red = color.red + (1 - color.red) * x,
        green = color.green + (1 - color.green) * x,
        blue = color.blue + (1 - color.blue) * x,
        alpha = color.alpha
      )

    Seq(
      withTint(0.2f),
      withTint(0.15f),
      withTint(0.1f),
      withTint(0.05f),
      color,
      withShade(0.05f),
      withShade(0.1f),
      withShade(0.15f),
      withShade(0.2f))
  }

  def getPlayerGfx(c: CharacterClass, sex: CharacterSex): String = {
    (c.value * 10 + sex.value).toString
  }

  def getDressStyleCount(c: CharacterClass, sex: CharacterSex): Int = c match {
    case CharacterClass.Sram if sex == CharacterSex.Male => 1
    case CharacterClass.Fog => 1
    case CharacterClass.Hupper => 10
    case _ => 5
  }

  def getHairStyleCount(c: CharacterClass): Int =
    if (c == CharacterClass.Elio) 1
    else 3

  def getHairGfx(c: CharacterClass, sex: CharacterSex, idx: Int): String =
    getStyleGfx("20", c, sex, idx)

  def getDressStyleGfx(
    c: CharacterClass,
    sex: CharacterSex,
    idx: Int): String =
    getStyleGfx("21", c, sex, idx)

  private[this] def getStyleGfx(
    prefix: String,
    c: CharacterClass,
    sex: CharacterSex,
    idx: Int): String = {
    f"$prefix${c.value}%02d${sex.value}00${idx + 1}%02d"
  }
}
