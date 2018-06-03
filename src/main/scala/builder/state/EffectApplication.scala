package builder.state

import builder.state.EffectApplication._
import cats.implicits._
import cats.kernel.Monoid
import monocle.Traversal
import org.bitbucket.wakfuthesaurus.shared.data.CharacteristicType.CharacteristicType
import org.bitbucket.wakfuthesaurus.shared.data.RuneType._
import org.bitbucket.wakfuthesaurus.shared.data._

import scala.language.postfixOps

final case class EffectApplication(
  update: UpdateFn = identity,
  modifiers: List[FinalModifier] = Nil,
  finalHpPercent: Int = 0,
  influence: Int = 0,
  robustness: Int = 0,
  acuity: Int = 0,
  velocity: Int = 0,
  vivacity: Int = 0
) {
  def compile(level: Int): Characteristics = {
    val onStartFight = update(Characteristics(level))
    modifiers.foldLeft(identity[Characteristics] _) { (acc, mod) =>
      acc andThen mod.apply(onStartFight, level)
    } andThen runeEffects andThen finalHp andThen limitations apply onStartFight

  }
  private[this] val limitations: UpdateFn = characteristics =>
    characteristics.copy(
      hp = if (characteristics.hp < 1) 1 else characteristics.hp,
      ap = if (characteristics.ap > 14) 14 else if (characteristics.ap < 0) 0 else characteristics.ap,
      mp = if (characteristics.mp > 8) 8 else if (characteristics.mp < 0) 0 else characteristics.mp,
      wp = if (characteristics.wp < 0) 0 else characteristics.wp
    )

  private[this] val finalHp: UpdateFn = characteristics =>
    characteristics.copy(hp = characteristics.hp * (1.0 + finalHpPercent / 100.0) toInt)

  private[this] val runeEffects: UpdateFn = { characteristics =>
    val block = math.min(robustness / 10, 10)
    val crit = math.min(influence / 10, 10)
    characteristics.copy(
      ap = characteristics.ap + (if (vivacity >= 140) 1 else 0),
      mp = characteristics.mp + (if (velocity >= 90) 1 else 0),
      range = characteristics.range + (if (acuity >= 60) 1 else 0),
      block = characteristics.block + block,
      criticalHits = characteristics.criticalHits + crit
    )
  }
}

object EffectApplication {
  type UpdateFn = Characteristics => Characteristics

  def justModifier(modifier: FinalModifier): EffectApplication =
    EffectApplication(modifiers = modifier :: Nil)

  sealed trait FinalModifier {
    def apply(characteristics: Characteristics, level: Int): UpdateFn
  }
  object FinalModifier {
    def instance(fn: (Characteristics, Int) => UpdateFn): FinalModifier = new FinalModifier {
      override def apply(characteristics: Characteristics, level: Int): UpdateFn = fn(characteristics, level)
    }

    final case class RelativePercent(source: CharacteristicType,
                                     target: CharacteristicType,
                                     percent: Int,
                                     max: Option[Int]) extends FinalModifier {
      override def apply(characteristics: Characteristics, level: Int): UpdateFn = {
        val value = {
          val v = EffectApplication.traversalOf(source).getAll(characteristics).head * percent / 100
          math.min(v, max.getOrElse(Int.MaxValue))
        }
        EffectApplication.traversalOf(target).modify(_ + value)
      }
    }

    final case class RelativeThreshold(source: CharacteristicType,
                                       target: CharacteristicType,
                                       threshold: Int,
                                       max: Option[Int]) extends FinalModifier {
      override def apply(characteristics: Characteristics, level: Int): UpdateFn = {
        val value = {
          val v = EffectApplication.traversalOf(source).getAll(characteristics).head / threshold
          math.min(v, max.getOrElse(Int.MaxValue))
        }
        EffectApplication.traversalOf(target).modify(_ + value)
      }
    }

    final case class Percent(target: CharacteristicType, percent: Int) extends FinalModifier {
      override def apply(characteristics: Characteristics, level: Int): UpdateFn = {
        val lens = EffectApplication.traversalOf(target)
        lens.modify(_ + lens.getAll(characteristics).head * (percent / 100))
      }
    }
  }

  val EffectBlackList = Set(159870, 159868, 162014)
  val empty: EffectApplication = EffectApplication()

  def fromCharacter(character: CharacterState): EffectApplication =
    fromSkills(character.skills) |+|
      fromCustom(character.customCharacteristics) |+|
      fromEquipment(character.equipment) |+|
      fromPassives(character.spells, character.level.level)

  def fromSkills(skills: Skills): EffectApplication = {
    import Characteristics._
    EffectApplication(
      ap.modify(_ + skills.major.ap) andThen
        mp.modify(_ + skills.major.mpAndDamage) andThen
        wp.modify(_ + skills.major.wp * 2) andThen
        damageInflicted.modify(_ + skills.major.damageInflicted * 10) andThen
        generalResist.modify(_ + skills.major.majorResistance * 50) andThen
        range.modify(_ + skills.major.rangeAndDamage) andThen
        control.modify(_ + skills.major.controlAndDamage * 2) andThen
        generalResist.modify(_ + skills.intelligence.elementalResist * 10) andThen
        generalMastery.modify {
          _ + skills.strength.elementalMastery * 5 +
          skills.major.mpAndDamage * 20 +
          skills.major.rangeAndDamage * 40 +
          skills.major.controlAndDamage * 40
        } andThen
        lock.modify(_ + skills.agility.lockAndDodge * 4 + skills.agility.lock * 6) andThen
        dodge.modify(_ + skills.agility.lockAndDodge * 4 + skills.agility.dodge * 6) andThen
        initiative.modify(_ + skills.agility.initiative * 4) andThen
        hp.modify(_ + skills.strength.healthPoints * 20) andThen
        meleeMastery.modify(_ + skills.strength.closeCombatMastery * 8) andThen
        distanceMastery.modify(_ + skills.strength.distanceMastery * 8) andThen
        singleTargetMastery.modify(_ + skills.strength.singleTargetMastery * 8) andThen
        areaMastery.modify(_ + skills.strength.areaMastery * 8) andThen
        criticalHits.modify(_ + skills.chance.criticalHits) andThen
        block.modify(_ + skills.chance.block) andThen
        criticalMastery.modify(_ + skills.chance.criticalMastery * 4) andThen
        criticalResist.modify(_ + skills.chance.criticalResist * 4) andThen
        rearMastery.modify(_ + skills.chance.rearMastery * 6) andThen
        rearResist.modify(_ + skills.chance.rearResist * 4) andThen
        healMastery.modify(_ + skills.chance.healingMastery * 6) andThen
        berserkMastery.modify(_ + skills.chance.berserkMastery * 8),
      finalHpPercent = skills.intelligence.percentHealthPoints * 4)
  }

  def fromCustom(custom: CustomCharacteristics): EffectApplication =
    EffectApplication({ characteristics =>
      characteristics.copy(
        hp = characteristics.hp + custom.hp,
        ap = characteristics.ap + custom.ap,
        mp = characteristics.mp + custom.mp,
        wp = characteristics.wp + custom.wp,
        airMastery = characteristics.airMastery + custom.airMastery,
        fireMastery = characteristics.fireMastery + custom.fireMastery,
        earthMastery = characteristics.earthMastery + custom.earthMastery,
        waterMastery = characteristics.waterMastery + custom.waterMastery,
        airResist = characteristics.airResist + custom.airResist,
        fireResist = characteristics.fireResist + custom.fireResist,
        earthResist = characteristics.earthResist + custom.earthResist,
        waterResist = characteristics.waterResist + custom.waterResist,
        damageInflicted = characteristics.damageInflicted + custom.damageInflicted,
        healsPerformed = characteristics.healsPerformed + custom.healsPerformed,
        criticalHits = characteristics.criticalHits + custom.criticalHits,
        block = characteristics.block + custom.block,
        initiative = characteristics.initiative + custom.initiative,
        range = characteristics.range + custom.range,
        dodge = characteristics.dodge + custom.dodge,
        lock = characteristics.lock + custom.lock,
        wisdom = characteristics.wisdom + custom.wisdom,
        prospecting = characteristics.prospecting + custom.prospecting,
        control = characteristics.control + custom.control,
        kitSkill = characteristics.kitSkill + custom.kitSkill,
        criticalMastery = characteristics.criticalMastery + custom.criticalMastery,
        criticalResist = characteristics.criticalResist + custom.criticalResist,
        rearMastery = characteristics.rearMastery + custom.rearMastery,
        rearResist = characteristics.rearResist + custom.rearResist,
        meleeMastery = characteristics.meleeMastery + custom.meleeMastery,
        distanceMastery = characteristics.distanceMastery + custom.distanceMastery,
        singleTargetMastery = characteristics.singleTargetMastery + custom.singleTargetMastery,
        areaMastery = characteristics.areaMastery + custom.areaMastery,
        healMastery = characteristics.healMastery + custom.healMastery,
        berserkMastery = characteristics.berserkMastery + custom.berserkMastery
      )
    }, finalHpPercent = custom.finalHpPercent)

  def fromPassives(spells: SpellsState, level: Int): EffectApplication = Monoid.combineAll {
    spells.passives.flatten.map(fromPassive(_, level))
  }

  def fromPassive(spell: Spell, level: Int): EffectApplication = {
    val skillLevel = spell.upgrades.takeWhile(_ <= level).length
    Monoid.combineAll(spell.effects.map(fromEffect(_, skillLevel)))
  }

  def fromEquipment(equipment: Equipment[SlotState]): EffectApplication = {
    val all = Equipment.weapons.getAll(equipment) ::: Equipment.gear.getAll(equipment).flatten
    Monoid.combineAll(all.map(fromSlot))
  }

  def fromSlot(slot: SlotState): EffectApplication = Monoid.combineAll {
    slot.item.equipEffects.map(fromEffect(_, slot.level, slot.variableElementMastery, slot.variableElementResist))
  } |+| Monoid.combineAll(slot.runes.flatMap(_.map(fromRune)))

  def fromEffect(
    effect: Effect,
    level: Int,
    variableElementMasteries: Seq[Element] = Element.values,
    variableElementResist: Seq[Element] = Element.values
  ): EffectApplication = {
    if (effect.effectRequirement.isEmpty && !EffectBlackList.contains(effect.id)) {
      effect match {
        case _: SingleEffect => fromSingleEffect(effect, level, variableElementMasteries, variableElementResist)
        case group: EffectGroup =>
          fromSingleEffect(group, level, variableElementMasteries, variableElementResist) |+|
            Monoid.combineAll(group.children.map(fromSingleEffect(_, level, variableElementMasteries, variableElementResist)))
      }
    } else empty
  }

  def fromSingleEffect(
    effect: Effect,
    level: Int,
    variableElementMasteries: Seq[Element] = Element.values,
    variableElementResist: Seq[Element] = Element.values
  ): EffectApplication = {

    def getParamUnsafe(idx: Int): Double =
      math.floor(effect.originalParams(idx * 2) + effect.originalParams(idx * 2 + 1) * level + 0.00001)

    def getMainIntParam: Int =
      if (effect.originalParams.isEmpty) 0
      else getParamUnsafe(0).toInt

    ActionType.byId(effect.action).collect {
      case ActionType.HpBoost =>
        apply(Characteristics.hp.modify(_ + getMainIntParam))
      case ActionType.HpDeboost ⇒
        apply(Characteristics.hp.modify(_ - getMainIntParam))
      case ActionType.ApBoost ⇒
        apply(Characteristics.ap.modify(_ + getMainIntParam))
      case ActionType.ApDeboost ⇒
        apply(Characteristics.ap.modify(_ - getMainIntParam))
      case ActionType.MpBoost ⇒
        apply(Characteristics.mp.modify(_ + getMainIntParam))
      case ActionType.MpDeboost ⇒
        apply(Characteristics.mp.modify(_ - getMainIntParam))
      case ActionType.WpBoost ⇒
        apply(Characteristics.wp.modify(_ + getMainIntParam))
      case ActionType.WpDeboost ⇒
        apply(Characteristics.wp.modify(_ - getMainIntParam))
      case ActionType.RangeGain ⇒
        apply(Characteristics.range.modify(_ + getMainIntParam))
      case ActionType.RangeLoss ⇒
        apply(Characteristics.range.modify(_ - getMainIntParam))
      case ActionType.ControlGain ⇒
        apply(Characteristics.control.modify(_ + getMainIntParam))
      case ActionType.ControlLoss ⇒
        apply(Characteristics.control.modify(_ - getMainIntParam))
      case ActionType.BlockGain ⇒
        apply(Characteristics.block.modify(_ + getMainIntParam))
      case ActionType.BlockLoss ⇒
        apply(Characteristics.block.modify(_ - getMainIntParam))
      case ActionType.CriticalHitGain ⇒
        apply(Characteristics.criticalHits.modify(_ + getMainIntParam))
      case ActionType.CriticalHitLoss ⇒
        apply(Characteristics.criticalHits.modify(_ - getMainIntParam))
      case ActionType.CriticalMasteryGain ⇒
        apply(Characteristics.criticalMastery.modify(_ + getMainIntParam))
      case ActionType.CritMasteryLoss ⇒
        apply(Characteristics.criticalMastery.modify(_ - getMainIntParam))
      case ActionType.RearMasteryGain ⇒
        apply(Characteristics.rearMastery.modify(_ + getMainIntParam))
      case ActionType.RearMasteryLoss ⇒
        apply(Characteristics.rearMastery.modify(_ - getMainIntParam))
      case ActionType.HealGain ⇒
        apply(Characteristics.healMastery.modify(_ + getMainIntParam))
      case ActionType.HealLoss ⇒
        apply(Characteristics.healMastery.modify(_ - getMainIntParam))
      case ActionType.AoeMasteryGain ⇒
        apply(Characteristics.areaMastery.modify(_ + getMainIntParam))
      case ActionType.AoeMasteryLoss ⇒
        apply(modifiers = FinalModifier.Percent(CharacteristicType.AreaMastery, -getMainIntParam) :: Nil)
      case ActionType.SingleTargetMasteryGain ⇒
        apply(Characteristics.singleTargetMastery.modify(_ + getMainIntParam))
      case ActionType.SingleTargetMasteryLoss ⇒
        apply(modifiers = FinalModifier.Percent(CharacteristicType.SingleTargetMastery, -getMainIntParam) :: Nil)
      case ActionType.MeleeMasteryGain ⇒
        apply(Characteristics.meleeMastery.modify(_ + getMainIntParam))
      case ActionType.MeleeMasteryLoss ⇒
        apply(modifiers = FinalModifier.Percent(CharacteristicType.MeleeMastery, -getMainIntParam) :: Nil)
      case ActionType.DistanceMasteryGain ⇒
        apply(Characteristics.distanceMastery.modify(_ + getMainIntParam))
      case ActionType.DistanceMasteryLoss ⇒
        apply(modifiers = FinalModifier.Percent(CharacteristicType.DistanceMastery, -getMainIntParam) :: Nil)
      case ActionType.BerserkMasteryGain ⇒
        apply(Characteristics.berserkMastery.modify(_ + getMainIntParam))
      case ActionType.BerserkMasteryLoss ⇒
        apply(modifiers = FinalModifier.Percent(CharacteristicType.BerserkMastery, -getMainIntParam) :: Nil)
      case ActionType.DodgeGain ⇒
        apply(Characteristics.dodge.modify(_ + getMainIntParam))
      case ActionType.DodgeLoss ⇒
        apply(Characteristics.dodge.modify(_ - getMainIntParam))
      case ActionType.LockGain ⇒
        apply(Characteristics.lock.modify(_ + getMainIntParam))
      case ActionType.LockLoss ⇒
        apply(Characteristics.lock.modify(_ - getMainIntParam))
      case ActionType.InitiativeGain ⇒
        apply(Characteristics.initiative.modify(_ + getMainIntParam))
      case ActionType.InitiativeLoss ⇒
        apply(Characteristics.initiative.modify(_ - getMainIntParam))
      case ActionType.KitSkillGain ⇒
        apply(Characteristics.kitSkill.modify(_ + getMainIntParam))
      case ActionType.ProspectingGain ⇒
        apply(Characteristics.prospecting.modify(_ + getMainIntParam))
      case ActionType.WisdomGain ⇒
        apply(Characteristics.wisdom.modify(_ + getMainIntParam))
      case ActionType.CriticalResGain ⇒
        apply(Characteristics.criticalResist.modify(_ + getMainIntParam))
      case ActionType.RearResistGain ⇒
        apply(Characteristics.rearResist.modify(_ + getMainIntParam))
      case ActionType.GeneralMasteryGain ⇒
        apply(Characteristics.generalMastery.modify(_ + getMainIntParam))
      case ActionType.GeneralMasteryLoss ⇒
        apply(Characteristics.generalMastery.modify(_ - getMainIntParam))
      case ActionType.GeneralResistGain ⇒
        apply(Characteristics.generalResist.modify(_ + getMainIntParam))
      case ActionType.GeneralResistLoss ⇒
        apply(Characteristics.generalResist.modify(_ - getMainIntParam))
      case ActionType.EarthMasteryGain ⇒
        apply(Characteristics.earthMastery.modify(_ + getMainIntParam))
      case ActionType.FireMasteryGain ⇒
        apply(Characteristics.fireMastery.modify(_ + getMainIntParam))
      case ActionType.WaterMasteryGain ⇒
        apply(Characteristics.waterMastery.modify(_ + getMainIntParam))
      case ActionType.AirMasteryGain ⇒
        apply(Characteristics.airMastery.modify(_ + getMainIntParam))
      case ActionType.EarthResistGain ⇒
        apply(Characteristics.earthResist.modify(_ + getMainIntParam))
      case ActionType.FireResistGain ⇒
        apply(Characteristics.fireResist.modify(_ + getMainIntParam))
      case ActionType.WaterResistGain ⇒
        apply(Characteristics.waterResist.modify(_ + getMainIntParam))
      case ActionType.AirResistGain ⇒
        apply(Characteristics.airResist.modify(_ + getMainIntParam))
      case ActionType.VariableElementsMasteryGain ⇒
        val lenses = variableElementMasteries.take(getParamUnsafe(1).toInt).collect {
          case Element.Air => Characteristics.airMastery
          case Element.Fire => Characteristics.fireMastery
          case Element.Earth => Characteristics.earthMastery
          case Element.Water => Characteristics.waterMastery
        }
        apply(Traversal.applyN(lenses: _*).modify(_ + getMainIntParam))
      case ActionType.VariableElementsResGain ⇒
        val lenses = variableElementResist.take(getParamUnsafe(1).toInt).collect {
          case Element.Air => Characteristics.airResist
          case Element.Fire => Characteristics.fireResist
          case Element.Earth => Characteristics.earthResist
          case Element.Water => Characteristics.waterResist
        }
        apply(Traversal.applyN(lenses: _*).modify(_ + getMainIntParam))
      case ActionType.DamageInflictedGain =>
        apply(Characteristics.damageInflicted.modify(_ + getMainIntParam))
      case ActionType.DamageInflictedLoss =>
        apply(Characteristics.damageInflicted.modify(_ - getMainIntParam))
      case ActionType.HealsPerformedGain =>
        apply(Characteristics.healsPerformed.modify(_ + getMainIntParam))
      case ActionType.HealsPerformedLoss =>
        apply(Characteristics.healsPerformed.modify(_ - getMainIntParam))
      case ActionType.ApRemovalChanceGain =>
        apply(Characteristics.apRemoval.modify(_ + getMainIntParam))
      case ActionType.MpRemovalChanceGain =>
        apply(Characteristics.mpRemoval.modify(_ + getMainIntParam))
      case ActionType.AddCharacValueToAnotherInRealTime =>
        val value = getParamUnsafe(0).toInt
        val destCharac = getParamUnsafe(1).toInt
        (CharacteristicType.byId(value), CharacteristicType.byId(destCharac)).mapN { (src, dst) =>
          val percentToCopy =
            if (effect.originalParams.length >= 8) getParamUnsafe(3).toInt
            else 100
          val max =
            if (effect.originalParams.length >= 10) getParamUnsafe(4).toInt
            else 0
          apply(modifiers =
            FinalModifier.RelativePercent(src, dst, percentToCopy, if (max == 0) None else Some(max)) :: Nil)
        }.getOrElse(empty)
      case ActionType.AddCharacValueToAnotherInRealTimeWithThreshold =>
        val value = getParamUnsafe(0).toInt
        val destCharac = getParamUnsafe(1).toInt
        (CharacteristicType.byId(value), CharacteristicType.byId(destCharac)).mapN { (src, dst) =>
          val threshold =
            if (effect.originalParams.length >= 8) getParamUnsafe(3).toInt
            else 1
          val max =
            if (effect.originalParams.length >= 10) getParamUnsafe(4).toInt
            else 0
          apply(modifiers =
            FinalModifier.RelativeThreshold(src, dst, threshold, if (max == 0) None else Some(max)) :: Nil)
        }.getOrElse(empty)
      case ActionType.UpdateHpMaxPercentModifier =>
        apply(finalHpPercent = getMainIntParam)
      case _ if effect.id == 161950 => // sublimation
        val sublimation = FinalModifier.instance { (ch, _) =>
          if (ch.range >= 2) Characteristics.mp.modify(_ + 1)
          else identity
        }
        justModifier(sublimation)
    }.getOrElse(empty)
  }

  def fromRune(rune: Rune): EffectApplication = rune.`type` match {
    case RuneType.Damage =>
      apply(Characteristics.generalMastery.modify(_ + rune.level))
    case RuneType.Vitality =>
      apply(Characteristics.hp.modify(_ + rune.level * 4))
    case RuneType.Resistance =>
      apply(Characteristics.generalResist.modify(_ + rune.level))
    case RuneType.Initiative =>
      apply(Characteristics.initiative.modify(_ + rune.level * 2))
    case RuneType.Lock =>
      apply(Characteristics.lock.modify(_ + rune.level * 2))
    case RuneType.Dodge =>
      apply(Characteristics.dodge.modify(_ + rune.level * 2))
    case RuneType.Destruction ⇒
      apply(Characteristics.areaMastery.modify(_ + rune.level * 2))
    case RuneType.Precision ⇒
      apply(Characteristics.singleTargetMastery.modify(_ + rune.level * 2))
    case RuneType.Distance ⇒
      apply(Characteristics.distanceMastery.modify(_ + rune.level * 2))
    case RuneType.Fury ⇒
      apply(Characteristics.berserkMastery.modify(_ + rune.level * 3))
    case RuneType.Melee ⇒
      apply(Characteristics.meleeMastery.modify(_ + rune.level * 3))
    case RuneType.Altruism ⇒
      apply(Characteristics.healMastery.modify(_ + rune.level * 3))
    case RuneType.Audacity ⇒
      apply(Characteristics.criticalMastery.modify(_ + rune.level * 2))
    case RuneType.Sneakiness ⇒
      apply(Characteristics.rearMastery.modify(_ + rune.level * 2))
    case RuneType.Life ⇒
      apply(Characteristics.hp.modify(_ + rune.level * 6))
    case RuneType.Influence ⇒
      apply(influence = rune.level)
    case RuneType.Robustness ⇒
      apply(robustness = rune.level)
    case RuneType.Acuity ⇒
      apply(acuity = rune.level)
    case RuneType.Velocity ⇒
      apply(velocity = rune.level)
    case RuneType.Vivacity ⇒
      apply(vivacity = rune.level)
    case SurgicalPrecision =>
      val mod = FinalModifier.instance { (base, _) =>
        if (base.range <= 1) Characteristics.healsPerformed.modify(_ + 20)
        else identity
      }
      justModifier(mod)
    case Measure =>
      val mod = FinalModifier.instance { (base, _) =>
        if (base.block >= 40) Characteristics.criticalHits.modify(_ + 10)
        else identity
      }
      justModifier(mod)
    case Unraveling =>
      val mod = FinalModifier.instance { (base, _) =>
        val toTransfer = (base.criticalMastery * 0.5).toInt
        Characteristics.criticalMastery.modify(_ - toTransfer) andThen
          Characteristics.generalMastery.modify(_ + toTransfer)
      }
      justModifier(mod)
    case Inflexibility =>
      val mod = FinalModifier.instance { (base, _) =>
        if (base.ap <= 10) Characteristics.damageInflicted.modify(_ + 20)
        else identity
      }
      justModifier(mod)
    case Steadfast =>
      val mod = FinalModifier.instance { (base, _) =>
        if (base.criticalHits <= 10) Characteristics.damageInflicted.modify(_ + 10)
        else identity
      }
      justModifier(mod)
    case IronHealth =>
      apply(finalHpPercent = -40)
    case PositioningScience =>
      apply(Characteristics.generalResist.modify(_ + 50) andThen Characteristics.rearResist.modify(_ - 100))
    case Brutality =>
      val mod = FinalModifier.instance { (base, _) =>
        if (base.areaMastery > base.meleeMastery)
          Characteristics.areaMastery.modify(_ + base.meleeMastery)
        else identity
      }
      justModifier(mod)
    case HerculeanStrength =>
      val mod = FinalModifier.instance { (base, lvl) =>
        if (base.ap % 2 != 0) {
          val buff = (lvl * 1.5).toInt
          Characteristics.dodge.modify(_ + buff) andThen Characteristics.lock.modify(_ + buff)
        }
        else identity
      }
      justModifier(mod)
    case _ => empty
  }

  def traversalOf(tpe: CharacteristicType): Traversal[Characteristics, Int] =
    tpe match {
        case CharacteristicType.Hp => Characteristics.hp.asTraversal
        case CharacteristicType.Ap => Characteristics.ap.asTraversal
        case CharacteristicType.Mp => Characteristics.mp.asTraversal
        case CharacteristicType.Wp => Characteristics.wp.asTraversal
        case CharacteristicType.Lock => Characteristics.lock.asTraversal
        case CharacteristicType.Dodge => Characteristics.dodge.asTraversal
        case CharacteristicType.CriticalChance => Characteristics.criticalHits.asTraversal
        case CharacteristicType.Range => Characteristics.range.asTraversal
        case CharacteristicType.Control => Characteristics.control.asTraversal
        case CharacteristicType.RearMastery => Characteristics.rearMastery.asTraversal
        case CharacteristicType.RearResist => Characteristics.rearResist.asTraversal
        case CharacteristicType.HealsPerformed => Characteristics.healsPerformed.asTraversal
        case CharacteristicType.ElementalMastery =>
          Traversal.applyN(Characteristics.fireMastery, Characteristics.waterMastery, Characteristics.earthMastery, Characteristics.airMastery)
        case CharacteristicType.FireMastery => Characteristics.fireMastery.asTraversal
        case CharacteristicType.WaterMastery => Characteristics.waterMastery.asTraversal
        case CharacteristicType.EarthMastery => Characteristics.earthMastery.asTraversal
        case CharacteristicType.AirMastery => Characteristics.airMastery.asTraversal
        case CharacteristicType.ElementalResist =>
          Traversal.applyN(Characteristics.fireResist,Characteristics.waterResist, Characteristics.earthResist, Characteristics.airResist)
        case CharacteristicType.FireResist => Characteristics.fireResist.asTraversal
        case CharacteristicType.WaterResist => Characteristics.waterResist.asTraversal
        case CharacteristicType.EarthResist => Characteristics.earthResist.asTraversal
        case CharacteristicType.AirResist => Characteristics.airResist.asTraversal
        case CharacteristicType.Prospecting => Characteristics.prospecting.asTraversal
        case CharacteristicType.Initiative => Characteristics.initiative.asTraversal
        case CharacteristicType.Wisdom => Characteristics.wisdom.asTraversal
        case CharacteristicType.DamageInflicted => Characteristics.damageInflicted.asTraversal
        case CharacteristicType.DamageReduced => ???
        case CharacteristicType.CriticalMastery => Characteristics.criticalMastery.asTraversal
        case CharacteristicType.Block => Characteristics.block.asTraversal
        case CharacteristicType.MeleeMastery => Characteristics.meleeMastery.asTraversal
        case CharacteristicType.DistanceMastery => Characteristics.distanceMastery.asTraversal
        case CharacteristicType.SingleTargetMastery => Characteristics.singleTargetMastery.asTraversal
        case CharacteristicType.AreaMastery => Characteristics.areaMastery.asTraversal
        case CharacteristicType.BerserkMastery => Characteristics.berserkMastery.asTraversal
    }

  implicit val monoid: Monoid[EffectApplication] =
    new Monoid[EffectApplication] {
      override val empty: EffectApplication = EffectApplication.empty

      override def combine(
        x: EffectApplication,
        y: EffectApplication
      ): EffectApplication =
        EffectApplication(
          update = x.update andThen y.update,
          x.modifiers ::: y.modifiers,
          x.finalHpPercent + y.finalHpPercent,
          x.influence + y.influence,
          x.robustness + y.robustness,
          x.acuity + y.acuity,
          x.velocity + y.velocity,
          x.vivacity + y.vivacity
        )
    }
}