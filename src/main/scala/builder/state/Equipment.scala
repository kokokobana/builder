package builder.state

import cats.Functor
import cats.data.Ior
import cats.instances.list._
import cats.kernel.Semigroup
import monocle.macros.Lenses
import monocle.{Fold, Getter, Traversal}
import org.bitbucket.wakfuthesaurus.shared.data.ItemType._
import org.bitbucket.wakfuthesaurus.shared.data.{Element, Item, ItemType, RuneType}
import shapeless.syntax.std.tuple._

@Lenses
final case class Equipment[T](
  headgear: Option[T] = None,
  cape: Option[T] = None,
  amulet: Option[T] = None,
  epaulettes: Option[T] = None,
  breastplate: Option[T] = None,
  belt: Option[T] = None,
  leftRing: Option[T] = None,
  rightRing: Option[T] = None,
  boots: Option[T] = None,
  pet: Option[T] = None,
  costume: Option[T] = None,
  weapon: Weapons[T] = Weapons.Bare,
  insignia: Option[T] = None,
  mount: Option[T] = None
)

object Equipment {
  val weapons: Fold[Equipment[SlotState], SlotState] =
    Getter[Equipment[SlotState], List[SlotState]](_.weapon.both).composeFold(Fold.fromFoldable[List, SlotState])

  val gear: Fold[Equipment[SlotState], Option[SlotState]] =
    Traversal.applyN(
      headgear[SlotState],
      cape[SlotState],
      amulet[SlotState],
      epaulettes[SlotState],
      breastplate[SlotState],
      belt[SlotState],
      leftRing[SlotState],
      rightRing[SlotState],
      boots[SlotState],
      pet[SlotState],
      costume[SlotState],
      insignia[SlotState],
      mount[SlotState]
    ).asFold
}

sealed trait Weapons[+T] {
  def left: Option[T]
  def right: Option[T]
  def both: List[T]
}
object Weapons {
  final case class TwoHanded[T](value: T) extends Weapons[T] {
    override def left: Option[T] = Some(value)
    override def right: Option[T] = Some(value)
    override def both: List[T] = List(value)
  }
  final case class OneHanded[T](state: Ior[T, T]) extends Weapons[T] {
    override def left: Option[T] = state.left
    override def right: Option[T] = state.right
    override def both: List[T] = state.pad.to[List].flatten
  }
  case object Bare extends Weapons[Nothing] {
    override def left: Option[Nothing] = None
    override def right: Option[Nothing] = None
    override def both: List[Nothing] = Nil
  }

  def combine(a: Weapons[SlotState], b: Weapons[SlotState]): Weapons[SlotState] =
    b match {
      case TwoHanded(_) => b
      case OneHanded(state) =>
        a match {
          case OneHanded(st) =>
            val alwaysRightSemigroup: Semigroup[SlotState] = Semigroup.instance((_, a) => a)
            OneHanded(st.combine(state)(alwaysRightSemigroup, alwaysRightSemigroup))
          case _ => b
        }
      case Bare => Bare
    }

  def remove(defaultRightHand: Boolean, weapons: Weapons[SlotState]): Weapons[SlotState] = weapons match {
    case TwoHanded(_) => Weapons.Bare
    case OneHanded(Ior.Both(_, r)) if !defaultRightHand => OneHanded(Ior.Right(r))
    case OneHanded(Ior.Both(l, _)) => OneHanded(Ior.Left(l))
    case _ => Bare
  }

  def apply(slot: SlotState): Option[Weapons[SlotState]] =
    ItemType.withValueOpt(slot.item.typeId).collect {
      case ItemType.Dagger | ItemType.Shield => OneHanded(Ior.Left(slot))
      case ItemType.Cards | ItemType.Hand | ItemType.OneHandStaff |
           ItemType.OneHandSword | ItemType.Wand => OneHanded(Ior.Right(slot))
      case ItemType.Axe | ItemType.Bow | ItemType.Hammer | ItemType.Shovel |
           ItemType.TwoHandStaff | ItemType.TwoHandSword => TwoHanded(slot)
    }

  def setter(defaultRightHand: Boolean): Option[SlotState] => Equipment[SlotState] => Equipment[SlotState] = slot => eq =>
    slot.flatMap(apply)
      .fold(eq.copy(weapon = remove(defaultRightHand, eq.weapon)))(v => eq.copy(weapon = combine(eq.weapon, v)))

  implicit val functor: Functor[Weapons] = new Functor[Weapons] {
    override def map[A, B](fa: Weapons[A])(f: A => B): Weapons[B] =
      fa match {
        case TwoHanded(state) => TwoHanded(f(state))
        case OneHanded(state) => OneHanded(state.bimap(f, f))
        case Bare => Bare
      }
  }
}

@Lenses
final case class SlotState(
  item: Item,
  level: Int,
  variableElementMastery: List[Element],
  variableElementResist: List[Element],
  runes: List[Option[Rune]]
) {
  def getRuneSlotType: Option[RuneSlotType] =
    ItemType.withValueOpt(item.typeId).flatMap {
      case Helmet | Ring | Dagger | Axe | Wand | OneHandSword | Shovel |
           OneHandStaff | Hammer | Hand | Bow | TwoHandSword | TwoHandStaff |
           Cards ⇒
        Some(RuneSlotType.Offensive)
      case Cape | Breastplate | Amulet | Epaulettes | Shield ⇒ Some(RuneSlotType.Defensive)
      case Belt | Boots ⇒ Some(RuneSlotType.Supportive)
      case _ ⇒ None
    }
}

object SlotState {
  def apply(
    item: Item,
    variableElementMastery: List[Element],
    variableElementResist: List[Element],
    runes: List[Option[Rune]]
  ): SlotState =
    apply(
      item,
      initializedLevel(item),
      variableElementMastery,
      variableElementResist,
      List.fill(getRuneCount(item))(None)
    )

  def initializedLevel(item: Item): Int =
    if (builder.util.item.makabraItemIds.contains(item.id)) 100
    else ItemType.withValueOpt(item.typeId).collect {
      case ItemType.Pet => 50
      case ItemType.Mount => 50
    }.getOrElse(item.level)

  def getRuneCount(item: Item): Int =
    ItemType.withValueOpt(item.typeId).collect {
      case Helmet | Boots if item.level < 40 ⇒ 0
      case Helmet | Boots if item.level < 80 ⇒ 1
      case Helmet | Boots if item.level < 120 ⇒ 2
      case Helmet | Boots ⇒ 3
      case Ring | Belt | Amulet | Epaulettes if item.level < 80 ⇒ 0
      case Ring | Belt | Amulet | Epaulettes if item.level < 120 ⇒ 1
      case Ring | Belt | Amulet | Epaulettes ⇒ 2
      case Cape | Breastplate if item.level < 80 ⇒ 1
      case Cape | Breastplate if item.level < 120 ⇒ 2
      case Cape | Breastplate ⇒ 3
      case Shield | Dagger if item.level < 40 ⇒ 0
      case Shield | Dagger ⇒ 1
      case Wand | OneHandSword | OneHandStaff | Hand | Cards if item.level < 40 ⇒ 0
      case Wand | OneHandSword | OneHandStaff | Hand | Cards if item.level < 120 ⇒ 1
      case Wand | OneHandSword | OneHandStaff | Hand | Cards ⇒ 2
      case Axe | Shovel | Hammer | Bow | TwoHandSword | TwoHandStaff if item.level < 40 ⇒ 0
      case Axe | Shovel | Hammer | Bow | TwoHandSword | TwoHandStaff if item.level < 120 ⇒ 2
      case Axe | Shovel | Hammer | Bow | TwoHandSword | TwoHandStaff ⇒ 3
    }.getOrElse(0)
}

final case class Rune(`type`: RuneType, level: Int)
