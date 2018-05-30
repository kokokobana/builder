package builder.util

import builder.lang.Translation
import org.bitbucket.wakfuthesaurus.shared.data.{ItemRarity, ItemType, RuneType}

object item {
  def isRarityRelic(rarity: Int): Boolean =
    rarity == ItemRarity.Relic.value

  def isRarityEpic(rarity: Int): Boolean =
    rarity == ItemRarity.Epic.value

  val makabraItemIds: Set[Int] =
    Set(12178, 12183, 12184, 12185, 12186, 12187, 12188, 12189, 12190, 12191, 12192, 12193, 12194, 12195, 12196, 12197)

  def getTypeIconId(typeId: Int): Option[Int] =
    typeId match {
      case ItemType.Axe.value | ItemType.Shovel.value | ItemType.Hammer.value |
           ItemType.Bow.value | ItemType.TwoHandSword.value |
           ItemType.TwoHandStaff.value ⇒
        Some(519)
      case ItemType.Wand.value | ItemType.OneHandSword.value |
           ItemType.OneHandStaff.value | ItemType.Hand.value |
           ItemType.Cards.value | ItemType.Dagger.value =>
        Some(518)
      case ItemType.Shield.value => Some(520)
      case ItemType.Ring.value => Some(103)
      case ItemType.Boots.value => Some(119)
      case ItemType.Amulet.value => Some(120)
      case ItemType.Cape.value => Some(132)
      case ItemType.Belt.value => Some(133)
      case ItemType.Helmet.value => Some(134)
      case ItemType.Breastplate.value => Some(136)
      case ItemType.Epaulettes.value => Some(138)
      case ItemType.Pet.value => Some(582)
      case ItemType.Mount.value => Some(611)
      case _ => None
    }

  def formatRune(rt: RuneType, translation: Translation, lvl: Int): String = {
    import RuneType._
    rt match {
      case Damage =>
        s"$lvl ${translation.characteristic("elemental_mastery")}"
      case Resistance =>
        s"$lvl ${translation.characteristic("elemental_resist")}"
      case Initiative =>
        s"${2 * lvl} ${translation.characteristic("initiative")}"
      case Lock =>
        s"${2 * lvl} ${translation.characteristic("lock")}"
      case Dodge =>
        s"${2 * lvl} ${translation.characteristic("dodge")}"
      case Destruction =>
        s"${2 * lvl} ${translation.characteristic("area_mastery")}"
      case Precision =>
        s"${2 * lvl} ${translation.characteristic("st_mastery")}"
      case Distance =>
        s"${2 * lvl} ${translation.characteristic("distance_mastery")}"
      case Fury =>
        s"${3 * lvl} ${translation.characteristic("berserk_mastery")}"
      case Melee =>
        s"${2 * lvl} ${translation.characteristic("melee_mastery")}"
      case Altruism =>
        s"${3 * lvl} ${translation.characteristic("heal_mastery")}"
      case Audacity =>
        s"${2 * lvl} ${translation.characteristic("crit_mastery")}"
      case Sneakiness =>
        s"${2 * lvl} ${translation.characteristic("rear_mastery")}"
      case Atrophy =>
        s"$lvl${translation.skill("%_ap_mp_removal")}"
      case Determination =>
        s"$lvl${translation.skill("%_ap_mp_resist")}"
      case Life =>
        s"${6 * lvl} ${translation.characteristic("hp")}"
      case Influence =>
        s"$lvl ${translation.itemRune("influence")}"
      case Robustness =>
        s"$lvl ${translation.itemRune("robustness")}"
      case Acuity =>
        s"$lvl ${translation.itemRune("acuity")}"
      case Velocity =>
        s"$lvl ${translation.itemRune("velocity")}"
      case Vivacity =>
        s"$lvl ${translation.itemRune("vivacity")}"
      case Vitality =>
        s"${4 * lvl} ${translation.characteristic("hp")}"
    }
  }
}
