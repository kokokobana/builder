package builder.state

import monocle.Traversal
import monocle.macros.Lenses

@Lenses
final case class Characteristics private(
  hp: Int,
  ap: Int,
  mp: Int,
  wp: Int,
  waterMastery: Int,
  waterResist: Int,
  earthMastery: Int,
  earthResist: Int,
  airMastery: Int,
  airResist: Int,
  fireMastery: Int,
  fireResist: Int,
  damageInflicted: Int,
  healsPerformed: Int,
  criticalHits: Int,
  block: Int,
  initiative: Int,
  range: Int,
  dodge: Int,
  lock: Int,
  wisdom: Int,
  prospecting: Int,
  control: Int,
  kitSkill: Int,
  criticalMastery: Int,
  criticalResist: Int,
  rearMastery: Int,
  rearResist: Int,
  meleeMastery: Int,
  distanceMastery: Int,
  singleTargetMastery: Int,
  areaMastery: Int,
  healMastery: Int,
  berserkMastery: Int,
  apRemoval: Int,
  apResist: Int,
  mpRemoval: Int,
  mpResist: Int
)

object Characteristics {
  val generalMastery: Traversal[Characteristics, Int] =
    Traversal.applyN(fireMastery, airMastery, earthMastery, waterMastery)

  val generalResist: Traversal[Characteristics, Int] =
    Traversal.applyN(fireResist, airResist, earthResist, waterResist)

  def apply(level: Int): Characteristics =
    Characteristics(
      hp = 50 + level * 10,
      ap = 6,
      mp = 3,
      wp = 6,
      waterMastery = 0,
      waterResist = 0,
      earthMastery = 0,
      earthResist = 0,
      airMastery = 0,
      airResist = 0,
      fireMastery = 0,
      fireResist = 0,
      damageInflicted = 0,
      healsPerformed = 0,
      criticalHits = 3,
      block = 0,
      initiative = 0,
      range = 0,
      dodge = 0,
      lock = 0,
      wisdom = 0,
      prospecting = 0,
      control = 1,
      kitSkill = 0,
      criticalMastery = 0,
      criticalResist = 0,
      rearMastery = 0,
      rearResist = 0,
      meleeMastery = 0,
      distanceMastery = 0,
      singleTargetMastery = 0,
      areaMastery = 0,
      healMastery = 0,
      berserkMastery = 0,
      apRemoval = 0,
      apResist = 0,
      mpRemoval = 0,
      mpResist = 0
    )
}
