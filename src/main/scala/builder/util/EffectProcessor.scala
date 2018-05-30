package builder.util

import builder.state.Characteristics
import org.bitbucket.wakfuthesaurus.shared.data.{ActionType, Effect}

object EffectProcessor {
  final case class Modifiers(
    characteristics: Characteristics,
    resistance: Int,
    isArea: Boolean,
    isCritical: Boolean,
    isDistance: Boolean,
    isBackstab: Boolean,
    isBerserk: Boolean
  )

  private[this] def getAdditiveModifier(
    modifiers: Modifiers
  )(effect: Effect,
    idx: Int
  ): Int = {
    val characteristics = modifiers.characteristics

    def getHighestMastery: Int =
      Characteristics.generalMastery.getAll(characteristics).max

    def applyAreaMastery(v: Int) =
      if (modifiers.isArea) v + characteristics.areaMastery
      else v + characteristics.singleTargetMastery

    def applyCritMastery(v: Int) =
      if (modifiers.isCritical) v + characteristics.criticalMastery
      else v

    def applyDistanceMastery(v: Int) =
      if (modifiers.isDistance) v + characteristics.distanceMastery
      else v + characteristics.meleeMastery

    def applyRearMastery(v: Int) =
      if (modifiers.isBackstab) v + characteristics.rearMastery
      else v

    def applyBerserkMastery(v: Int) =
      if (modifiers.isBerserk) v + characteristics.berserkMastery
      else v

    def applyDamage =
      applyRearMastery _ compose
        applyBerserkMastery compose
        applyAreaMastery compose
        applyDistanceMastery compose
        applyCritMastery

    def applyHeal(v: Int) =
      applyAreaMastery _ compose
        applyDistanceMastery compose
        applyCritMastery apply (v + characteristics.healMastery)

    def applyShield(v: Int) =
      v + characteristics.singleTargetMastery + characteristics.meleeMastery

    ActionType
      .byId(effect.action)
      .collect {
        case ActionType.HpFireLoss | ActionType.RandomHpFireLoss =>
          applyDamage(characteristics.fireMastery)
        case ActionType.HpGainFire =>
          applyHeal(characteristics.fireMastery)
        case ActionType.HpEarthLoss | ActionType.RandomHpEarthLoss =>
          applyDamage(characteristics.earthMastery)
        case ActionType.HpGainEarth =>
          applyHeal(characteristics.earthMastery)
        case ActionType.HpWaterLoss | ActionType.RandomHpWaterLoss =>
          applyDamage(characteristics.waterMastery)
        case ActionType.HpGainWater =>
          applyHeal(characteristics.waterMastery)
        case ActionType.HpAirLoss | ActionType.RandomHpAirLoss =>
          applyDamage(characteristics.airMastery)
        case ActionType.HpGainAir =>
          applyHeal(characteristics.airMastery)
        case ActionType.HpStasisLoss =>
          applyDamage(getHighestMastery)
        case ActionType.HpLightLoss =>
          applyDamage(getHighestMastery)
        case ActionType.HpGainLight =>
          applyHeal(getHighestMastery)
        case ActionType.Push if idx == 1 && effect.originalParams.isDefinedAt(6) =>
          val value = effect.originalParams(6) match {
            case 1.0 => characteristics.fireMastery
            case 2.0 => characteristics.waterMastery
            case 3.0 => characteristics.earthMastery
            case 4.0 => characteristics.airMastery
            case _ => 0
          }
          applyBerserkMastery _ compose
            applyAreaMastery compose
            applyDistanceMastery compose
            applyCritMastery apply value
        case ActionType.ArmorElementalGain if effect.originalParams.isDefinedAt(2) =>
          val value = effect.originalParams(2) match {
            case 1.0 => characteristics.fireMastery
            case 2.0 => characteristics.waterMastery
            case 3.0 => characteristics.earthMastery
            case 4.0 => characteristics.airMastery
            case _ => 0
          }
          applyShield(value)
        case ActionType.HpBoostFunctionDmgBonus if effect.originalParams.isDefinedAt(0) =>
          val value = effect.originalParams.head match {
            case 1.0 => characteristics.airMastery
            case 2.0 => characteristics.earthMastery
            case 3.0 => characteristics.fireMastery
            case 4.0 => characteristics.waterMastery
            case _ => 0
          }
          applyAreaMastery _ compose
            applyDistanceMastery compose
            applyCritMastery apply value
        // hupper
        case _ if effect.id == 175218 || effect.id == 175552 =>
          applyDamage(getHighestMastery)

        // xel
        case _ if (effect.id == 164604 || effect.id == 164606) && idx == 1 =>
          applyDamage(characteristics.fireMastery)
        case _ if effect.id == 164131 || effect.id == 164699 =>
          applyDamage(characteristics.fireMastery)
        case _ if effect.id == 164136 || effect.id == 164704 =>
          applyDamage(characteristics.fireMastery)
        case _ if effect.id == 164467 || effect.id == 164731 =>
          applyDamage(characteristics.airMastery)
        case _ if effect.id == 164456 || effect.id == 164694 =>
          applyDamage(characteristics.airMastery)
        case _ if effect.id == 46731 && idx == 0 =>
          applyDamage(characteristics.airMastery)
        case _ if effect.id == 46736 && idx == 0 =>
          applyDamage(characteristics.airMastery)

        // sac
        // case tpe if effect.id == 161859 || effect.id == 162070 => // shield
        //   applyShield(characteristics.earthMastery)

        // case tpe if effect.id == 157265 || effect.id == 162048 =>
        //   applyDamage(characteristics.fireMastery)
        // case tpe if (effect.id == 161845 || effect.id == 161849) && idx == 1 =>
        //   applyDamage(characteristics.fireMastery)
        // case tpe if effect.id == 161976 =>
        //   applyDamage(characteristics.fireMastery)
        // case tpe if effect.id == 162226 || effect.id == 162293 =>
        //   applyDamage(characteristics.airMastery)
        // case tpe if effect.id == 161958 || effect.id == 162290 =>
        //   applyDamage(characteristics.airMastery)
        // case tpe if (effect.id == 162098 || effect.id == 162532) && idx == 1 =>
        //   applyDamage(characteristics.airMastery)
        // case tpe if effect.id == 162182 || effect.id == 162537 =>
        //   applyDamage(characteristics.airMastery)
        // case tpe if effect.id == 161751 || effect.id == 161838 =>
        //   applyHeal(characteristics.fireMastery)

        // rogue
        case _ if effect.id == 166405 || effect.id == 166407 =>
          applyDamage(characteristics.earthMastery)
        case _ if effect.id == 164511 || effect.id == 165105 =>
          applyDamage(characteristics.airMastery)

        // panda
        case _ if effect.id == 145871 || effect.id == 146304 => // shield
          applyShield(characteristics.earthMastery)

        // iop
        case _ if (effect.id == 152201 || effect.id == 152229) && idx == 1 =>
          applyDamage(characteristics.earthMastery)
        case _ if effect.id == 151572 || effect.id == 151633 =>
          applyDamage(characteristics.fireMastery)
        case _ if effect.id == 151608 || effect.id == 151844 =>
          applyDamage(characteristics.fireMastery)

        // fog
        case _ if effect.id == 167459 || effect.id == 168707 =>
          applyDamage(characteristics.earthMastery)

        // enu
        case _ if effect.id == 156770 || effect.id == 160952 =>
          applyDamage(characteristics.fireMastery)

        // eca
        case _ if (effect.id == 160877 || effect.id == 160878) && idx == 1 =>
          applyDamage(characteristics.waterMastery)

        // eni
        case _ if (effect.id == 166949 || effect.id == 166957) && idx == 0 =>
          applyHeal(characteristics.waterMastery)
      }
      .getOrElse(0)
  }

  private[this] def getMultiplicativeModifier(
    modifiers: Modifiers)(effect: Effect, idx: Int): Double = {
    val characteristics = modifiers.characteristics

    def applyBackstab(v: Double) =
      if (modifiers.isBackstab) v * 1.25
      else v

    def applyResist(v: Double) =
      v * (100 - modifiers.resistance) / 100.0

    def applyDamage =
      applyResist _ compose
        applyBackstab apply (100 + characteristics.damageInflicted)

    def applyHeal: Double =
      100 + characteristics.healsPerformed

    ActionType
      .byId(effect.action)
      .collect {
        case tpe if damageTypes.contains(tpe) => applyDamage
        case tpe if healTypes.contains(tpe) => applyHeal
        case ActionType.Push if idx == 1 =>
          applyResist(100 + characteristics.damageInflicted)
        // hupper
        case _ if effect.id == 175218 || effect.id == 175552 =>
          applyDamage
        // xel
        case _ if (effect.id == 164604 || effect.id == 164606) && idx == 1 =>
          applyDamage
        case _ if effect.id == 164131 || effect.id == 164699 =>
          applyDamage
        case _ if effect.id == 164136 || effect.id == 164704 =>
          applyDamage
        case _ if effect.id == 164467 || effect.id == 164731 =>
          applyDamage
        case _ if effect.id == 164456 || effect.id == 164694 =>
          applyDamage
        case _ if effect.id == 46731 && idx == 0 =>
          applyDamage
        case _ if effect.id == 46736 && idx == 0 =>
          applyDamage
        // sac
        // case tpe if effect.id == 157265 || effect.id == 162048 =>
        //   applyDamage
        // case tpe if (effect.id == 161845 || effect.id == 161849) && idx == 1 =>
        //   applyDamage
        // case tpe if effect.id == 161976 =>
        //   applyDamage
        // case tpe if effect.id == 162226 || effect.id == 162293 =>
        //   applyDamage
        // case tpe if effect.id == 161958 || effect.id == 162290 =>
        //   applyDamage
        // case tpe if (effect.id == 162098 || effect.id == 162532) && idx == 1 =>
        //   applyDamage
        // case tpe if effect.id == 162182 || effect.id == 162537 =>
        //   applyDamage
        // case tpe if effect.id == 161751 || effect.id == 161838 =>
        //   applyHeal

        // rogue
        case _ if effect.id == 166405 || effect.id == 166407 =>
          applyDamage
        case _ if effect.id == 164511 || effect.id == 165105 =>
          applyDamage

        // iop
        case _ if (effect.id == 152201 || effect.id == 152229) && idx == 1 =>
          applyDamage
        case _ if effect.id == 151572 || effect.id == 151633 =>
          applyDamage
        case _ if effect.id == 151608 || effect.id == 151844 =>
          applyDamage
        // fog
        case _ if effect.id == 167459 || effect.id == 168707 =>
          applyDamage
        // enu
        case _ if effect.id == 156770 || effect.id == 160952 =>
          applyDamage
        // eca
        case _ if (effect.id == 160877 || effect.id == 160878) && idx == 1 =>
          applyDamage
        // eni
        case _ if (effect.id == 166949 || effect.id == 166957) && idx == 0 =>
          applyHeal
      }
      .getOrElse(100)
  }

  private[this] val damageTypes: Set[ActionType.ActionType] = Set(
    ActionType.HpFireLoss,
    ActionType.HpEarthLoss,
    ActionType.HpWaterLoss,
    ActionType.HpAirLoss,
    ActionType.HpStasisLoss,
    ActionType.HpLightLoss,
    ActionType.RandomHpFireLoss,
    ActionType.RandomHpEarthLoss,
    ActionType.RandomHpWaterLoss,
    ActionType.RandomHpAirLoss)

  private[this] val healTypes: Set[ActionType.ActionType] = Set(
    ActionType.HpGainFire,
    ActionType.HpGainEarth,
    ActionType.HpGainWater,
    ActionType.HpGainAir,
    ActionType.HpGainLight)

  def process(modifiers: Modifiers)(effect: Effect, idx: Int)(
    base: Double): Double = {
    val additiveBonus = getAdditiveModifier(modifiers)(effect, idx)
    val multplicativeBonus = getMultiplicativeModifier(modifiers)(effect, idx)

    base * (100 + additiveBonus) / 100.0 * multplicativeBonus / 100.0
  }
}
