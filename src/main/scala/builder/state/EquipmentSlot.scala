package builder.state

import monocle.Lens

sealed abstract class EquipmentSlot(val icon: String) {
  def lens: Lens[Equipment[SlotState], Option[SlotState]]
}

object EquipmentSlot {
  case object Cape extends EquipmentSlot("cape") {
    override def lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.cape
  }
  case object Headgear extends EquipmentSlot("headgear") {
    override def lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.headgear
  }
  case object Epaulettes extends EquipmentSlot("epaulettes") {
    override def lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.epaulettes
  }
  case object Amulet extends EquipmentSlot("amulet") {
    override def lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.amulet
  }
  case object LeftRing extends EquipmentSlot("left_ring") {
    override def lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.leftRing
  }
  case object RightRing extends EquipmentSlot("right_ring") {
    override def lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.rightRing
  }
  case object LeftHand extends EquipmentSlot("left_hand") {
    override def lens: Lens[Equipment[SlotState], Option[SlotState]] =
      Lens[Equipment[SlotState], Option[SlotState]](_.weapon.left)(Weapons.setter)
  }
  case object RightHand extends EquipmentSlot("right_hand") {
    override val lens: Lens[Equipment[SlotState], Option[SlotState]] =
      Lens[Equipment[SlotState], Option[SlotState]](_.weapon.right)(Weapons.setter)
  }
  case object Belt extends EquipmentSlot("belt") {
    override val lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.belt
  }
  case object Breastplate extends EquipmentSlot("breastplate") {
    override val lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.breastplate
  }
  case object Boots extends EquipmentSlot("boots") {
    override val lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.boots
  }
  case object Emblem extends EquipmentSlot("emblem") {
    override val lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.insignia
  }
  case object Costume extends EquipmentSlot("costume") {
    override val lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.costume
  }
  case object Pet extends EquipmentSlot("pet") {
    override val lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.pet
  }
  case object Mount extends EquipmentSlot("pet") {
    override val lens: Lens[Equipment[SlotState], Option[SlotState]] = Equipment.mount
  }
}
