package builder.state

sealed trait RuneSlotType
object RuneSlotType {
  case object Offensive extends RuneSlotType
  case object Supportive extends RuneSlotType
  case object Defensive extends RuneSlotType
}
