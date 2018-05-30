package builder.state

sealed abstract class StaticActorStance(val animation: String, val flipped: Boolean)

object StaticActorStance {
  case object Front extends StaticActorStance("2_AnimStatique", false)
  case object Back extends StaticActorStance("6_AnimStatique", false)
  case object LeftProfile extends StaticActorStance("0_AnimStatique", false)
  case object RightProfile extends StaticActorStance("0_AnimStatique", true)
  case object LeftFront extends StaticActorStance("1_AnimStatique", false)
  case object RightBack extends StaticActorStance("5_AnimStatique", false)
  case object LeftBack extends StaticActorStance("5_AnimStatique", true)
  case object RightFront extends StaticActorStance("1_AnimStatique", true)

  val values = Seq(Front, LeftFront, LeftProfile, LeftBack, Back, RightBack, RightProfile, RightFront)

  def left(stance: StaticActorStance): StaticActorStance = {
    val idx = values.indexOf(stance)
    if (idx > 0) values(idx - 1)
    else values.last
  }

  def right(stance: StaticActorStance): StaticActorStance = {
    val idx = values.indexOf(stance)
    if (idx < values.length - 1) values(idx + 1)
    else values.head
  }
}
