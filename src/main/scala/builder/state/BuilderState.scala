package builder.state

import builder.Service.BuildResponse
import diode.data.Pot
import diode.{FastEq, UseValueEq}
import japgolly.scalajs.react.vdom.VdomNode
import monocle.macros.Lenses
import org.bitbucket.wakfuthesaurus.shared.data._

final case class BuilderState(
  character: CharacterState = CharacterState(level = CharacterLevelState(200)),
  characteristics: Characteristics = Characteristics(200),
  view: ViewState = ViewState(0),
  sideMenu: SideMenuState = SideMenuState(false),
  equipmentSearch: EquipmentSearchState = EquipmentSearchState(),
  classData: ClassDataState = ClassDataState.Loading,
  ringSelect: RingSelectState = RingSelectState(None),
  equipmentToast: EquipmentToastState = EquipmentToastState(None),
  spellToast: SpellToastState = SpellToastState(None),
  buildCodeDialog: BuildCodeDialogState = BuildCodeDialogState(Pot.empty)
)

final case class ViewState(activeIndex: Int)
final case class SideMenuState(open: Boolean)
final case class RingSelectState(alertItem: Option[SlotState])
final case class BuildCodeDialogState(state: Pot[BuildResponse])
final case class EquipmentToastState(toastText: Option[String])
final case class SpellToastState(toastText: Option[String])
final case class RuneDialogState()
final case class ItemWithCachedEffects(value: Item, effectList: VdomNode)
final case class ElementPriorityState(elements: List[Element])

final case class EquipmentSearchState(
  items: Seq[ItemWithCachedEffects] = Seq.empty,
  loading: Boolean = false,
  params: SearchParameters = SearchParameters(),
  total: Int = Int.MaxValue,
  elementPriorities: ElementPriorityState = ElementPriorityState(Element.values.take(4).toList)
) {
  def initializedSlot(item: Item): SlotState =
    SlotState(item, elementPriorities.elements, elementPriorities.elements, List.empty)
}

@Lenses
final case class AppearanceState(
  sex: CharacterSex = CharacterSex.Male,
  bodyIdx: Int = 0,
  hairIdx: Int = 0,
  hairColor: Color = Color(1, 1, 1, 1),
  skinColor: Color = Color(1, 1, 1, 1),
  pupilColor: Color = Color(1, 1, 1, 1),
  guildColor1: Color = Color(1, 1, 1, 1),
  guildColor2: Color = Color(1, 1, 1, 1)
)

final case class CharacterState(
  level: CharacterLevelState = CharacterLevelState(200),
  `class`: ClassState = ClassState(CharacterClass.Feca),
  customCharacteristics: CustomCharacteristics = CustomCharacteristics(),
  skills: Skills = Skills(),
  spells: SpellsState = SpellsState(),
  equipment: Equipment[SlotState] = Equipment[SlotState](),
  appearance: AppearanceState = AppearanceState()
)

final case class CharacterLevelState(level: Int)
final case class ClassState(`class`: CharacterClass)

sealed trait ClassDataState extends UseValueEq {
  def value: Option[CharacterBreed]
}
object ClassDataState {
  implicit val fastEq: FastEq[ClassDataState] = (a, b) => a.value.map(_.id) == b.value.map(_.id)

  case object Loading extends ClassDataState {
    override def value: Option[CharacterBreed] = None
  }
  final case class Loaded(breed: CharacterBreed) extends ClassDataState {
    override def value: Option[CharacterBreed] = Some(breed)
  }
}
