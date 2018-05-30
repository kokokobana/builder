package builder.state

import monocle.macros.Lenses
import shapeless.ops.hlist._
import shapeless.{Generic, HList, Poly2}

@Lenses
final case class Skills(
  major: Skills.Major = Skills.Major(),
  chance: Skills.Chance = Skills.Chance(),
  agility: Skills.Agility = Skills.Agility(),
  strength: Skills.Strength = Skills.Strength(),
  intelligence: Skills.Intelligence = Skills.Intelligence()
)

object Skills {
  @Lenses
  final case class Major(
    ap: Int = 0,
    mpAndDamage: Int = 0,
    rangeAndDamage: Int = 0,
    wp: Int = 0,
    controlAndDamage: Int = 0,
    damageInflicted: Int = 0,
    majorResistance: Int = 0
  )
  object Major extends SkillCategoryCompanion[Major] {
    def points(level: Int): Int = (level + 25) / 50
  }
  @Lenses
  final case class Chance(
    criticalHits: Int = 0,
    block: Int = 0,
    criticalMastery: Int = 0,
    rearMastery: Int = 0,
    berserkMastery: Int = 0,
    healingMastery: Int = 0,
    rearResist: Int = 0,
    criticalResist: Int = 0
  )
  object Chance extends SkillCategoryCompanion[Chance] {
    def points(level: Int): Int =
      if (level == 200) 50 else (level - 1) / 4
  }
  @Lenses
  final case class Agility(
    lock: Int = 0,
    dodge: Int = 0,
    initiative: Int = 0,
    lockAndDodge: Int = 0,
    apAndMpRemoval: Int = 0,
    apAndMpResist: Int = 0
  )
  object Agility extends SkillCategoryCompanion[Agility] {
    def points(level: Int): Int = level / 4
  }
  @Lenses
  final case class Strength(
    elementalMastery: Int = 0,
    singleTargetMastery: Int = 0,
    areaMastery: Int = 0,
    closeCombatMastery: Int = 0,
    distanceMastery: Int = 0,
    healthPoints: Int = 0
  )
  object Strength extends SkillCategoryCompanion[Strength] {
    def points(level: Int): Int = (level + 1) / 4
  }
  @Lenses
  final case class Intelligence(
    percentHealthPoints: Int = 0,
    elementalResist: Int = 0,
    barrier: Int = 0,
    healsReceived: Int = 0,
    armorHealthPoints: Int = 0
  )
  object Intelligence extends SkillCategoryCompanion[Intelligence] {
    def points(level: Int): Int = (level + 2) / 4
  }
}

trait SkillCategoryCompanion[T] {
  def points(level: Int): Int

  def remaining[L <: HList](value: T)(level: Int)
    (implicit gen: Generic.Aux[T, L], fold: LeftFolder.Aux[L, Int, internal.sum.type, Int]): Int =
    points(level) - gen.to(value).foldLeft(0)(internal.sum)
}

private object internal {
  object sum extends Poly2 {
    implicit val caseInt: Case.Aux[Int, Int, Int] = at[Int, Int](_ + _)
  }
}
