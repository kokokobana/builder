package builder

import builder.Service.{Build, BuildResponse}
import builder.lang.Translation
import builder.state.SpellsState.SpellLens
import builder.state._
import builder.util.EffectList
import cats.implicits._
import diode.Implicits.runAfterImpl
import diode._
import diode.data.{Pending, Pot, Ready}
import diode.react.ReactConnector
import org.bitbucket.wakfuthesaurus.shared.data._
import scalacss.ScalaCssReact._

import scala.Function.const
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object MainCircuit extends Circuit[BuilderState] with ReactConnector[BuilderState] {
  type HandleFunction[T] = PartialFunction[Any, ActionResult[T]]

  val params: Params = parseParams()

  val lang: Translation =
    params.lang match {
      case "en" => builder.lang.english
      case "fr" => builder.lang.french
      case "es" => builder.lang.spanish
      case "cn" => builder.lang.tchinese
      case "pt" => builder.lang.portuguese
    }
  val api = new Service(Config.apiUrl, Config.bucketUrl, params.lang)
  val context: Context = Context(lang, api, Config.bucketUrl)

  override protected def initialModel: BuilderState = BuilderState()

  override protected def actionHandler: HandlerFunction =
    composeHandlers(
      viewHandler,
      sideMenuHandler,
      classDataHandler,
      customCharacteristicHandler,
      characterHandler,
      characteristicsHandler,
      equipmentSearchHandler,
      skillsHandler,
      spellsHandler,
      equipmentHandler,
      equipmentToastHandler,
      ringSelectHandler,
      spellToastHandler,
      levelHandler,
      classHandler,
      elementPrioritiesHandler,
      buildCodeDialogHandler,
      appearanceHandler
    )

  val viewHandler: ActionHandler[BuilderState, ViewState] = new ActionHandler(zoomTo(_.view)) {
    override protected def handle: HandleFunction[BuilderState] = {
      case SetView(idx) => updated(ViewState(idx))
    }
  }

  val sideMenuHandler: ActionHandler[BuilderState, SideMenuState] = new ActionHandler(zoomTo(_.sideMenu)) {
    override protected def handle: HandleFunction[BuilderState] = {
      case SetSideMenu(open, false) =>
        updated(SideMenuState(open))
      case SetSideMenu(open, true) =>
        updatedSilent(SideMenuState(open))
    }
  }

  val classDataHandler: ActionHandler[BuilderState, ClassDataState] =
    new ActionHandler(zoomTo(_.classData)) {
    override protected def handle: HandleFunction[BuilderState] = {
      case LoadClassData(clazz) =>
        updated(ClassDataState.Loading, Effect {
          context.api.getBreed(clazz.value).map(ClassDataLoaded(_, fromBuild = false))
        })
      case ClassDataLoaded(breed, fromBuild) =>
        def defaultAppearance = {
          val sex = modelRW.root.value.character.appearance.sex
          val df = breed.definitions.find(_.sex == sex.value).get
          val hairColor =
            if(df.hairColors.isDefinedAt(df.defaultHairIndex))
              util.common.generateCharacterColorShades(df.hairColors(df.defaultHairIndex))(df.defaultHairFactor + 4)
            else Color(1, 1, 1, 1)
          val skinColor =
            if(df.skinColors.isDefinedAt(df.defaultSkinIndex))
              util.common.generateCharacterColorShades(df.skinColors(df.defaultSkinIndex))(df.defaultSkinFactor + 4)
            else Color(1, 1, 1, 1)
          val pupilColor =
            if(df.pupilColors.isDefinedAt(df.defaultPupilIndex))
              df.pupilColors(df.defaultPupilIndex)
            else Color(1, 1, 1, 1)
          AppearanceState(hairColor = hairColor, skinColor = skinColor, pupilColor = pupilColor)
        }
        val eff =
          if (!fromBuild)
            Effect.action(SetAppearance(defaultAppearance, silent = true)) >>
              Effect.action(ClassLoadComplete(Some(defaultAppearance)))
          else Effect.action(ClassLoadComplete(None))
        updated(ClassDataState.Loaded(breed), eff)
    }
  }

  val customCharacteristicHandler: ActionHandler[BuilderState, CustomCharacteristics] =
    new ActionHandler(zoomTo(_.character.customCharacteristics)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case UpdateCustomCharacteristics(modify) =>
          updated(modify(value), Effect.action(RecalculateCharacteristics))
      }
    }

  val skillsHandler: ActionHandler[BuilderState, Skills] =
    new ActionHandler(zoomTo(_.character.skills)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case UpdateSkills(modify) =>
          updated(modify(value), Effect.action(RecalculateCharacteristics))
      }
    }

  val spellsHandler: ActionHandler[BuilderState, SpellsState] =
    new ActionHandler(zoomTo(_.character.spells)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case UpdateSpells(modify) =>
          updated(modify(value), Effect.action(RecalculateCharacteristics))
        case SpellUsed(spell, lens, added) =>
          val text =
            if (added) s"${spell.name.getOrElse("")} added to deck"
            else s"${spell.name.getOrElse("")} removed from deck"
          effectOnly {
            Effect.action(UpdateSpells(lens.set(if(added) Some(spell) else None))) >>
              Effect.action(SetSpellToast(text, visible = true)) >>
              runAfter(650 millis)(SetSpellToast(text, visible = false))
          }
      }
    }

  val characteristicsHandler: ActionHandler[BuilderState, Characteristics] =
    new ActionHandler(zoomTo(_.characteristics)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetCharacteristics(characteristics) => updated(characteristics)
      }
    }

  val characterHandler: ActionHandler[BuilderState, CharacterState] =
    new ActionHandler(zoomTo(_.character)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetCharacterState(state, sideEffect) =>
          updated(state, sideEffect >> Effect.action(RecalculateCharacteristics))
        case RecalculateCharacteristics =>
          val newValue = EffectApplication.fromCharacter(value).compile(Characteristics(value.level.level))
          effectOnly(Effect.action(SetCharacteristics(newValue)))
        case ShareBuild => effectOnly {
          Effect.action(SetBuildCodeDialog(Pending())) >>
            Effect {
              api.saveBuild(Build.fromState(value))
                .map(resp => SetBuildCodeDialog(Ready(resp)))
            }
        }
        case LoadBuild(code) => effectOnly {
          Effect {
            for {
              resp <- api.getBuild(code)
              breed <- api.getBreed(resp.build.`class`.value)
            } yield BuildLoaded(breed, resp.build)
          }
        }
        case BuildLoaded(breed, build) => effectOnly {
          Effect(Build.toState(api)(breed, build)
            .map(SetCharacterState(_, Effect.action(ClassDataLoaded(breed, fromBuild = true)))))
        }
      }
    }

  val equipmentHandler: ActionHandler[BuilderState, Equipment[SlotState]] =
    new ActionHandler(zoomTo(_.character.equipment)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case UpdateEquipment(modify) => effectOnly(Effect.action(SetEquipment(modify(value))))
        case SetEquipment(value) => updated(value, Effect.action(RecalculateCharacteristics))
        case ItemClicked(slot) =>
          ItemType.withValueOpt(slot.item.typeId.toInt).map { tpe =>
            equipmentSlotSetter.lift.apply(tpe) match {
              case Some(set) =>
                val text = s"${slot.item.name.getOrElse("")} has been equipped"
                effectOnly {
                  Effect.action(UpdateEquipment(set(Some(slot)))) >>
                    Effect.action(SetEquipmentToast(text, visible = true)) >>
                    runAfter(650 millis)(SetEquipmentToast(text, visible = false))
                }
              case None =>
                tpe match {
                  case ItemType.Ring
                    if !(value.leftRing.exists(_.item.id === slot.item.id) ||
                      value.rightRing.exists(_.item.id === slot.item.id)) =>
                    effectOnly(Effect.action(SetRingSelect(Some(slot))))
                  case _ =>
                    noChange
                }
            }
          }.getOrElse(noChange)
      }
    }

  val equipmentSearchHandler: ActionHandler[BuilderState, EquipmentSearchState] =
    new ActionHandler(zoomTo(_.equipmentSearch)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetItemSearchParams(parameters) =>
          updated(value.copy(params = parameters, items = Seq.empty, total = Int.MaxValue),
            Effect.action(InitializeItemSearch))
        case InitializeItemSearch =>
          if (value.items.isEmpty) effectOnly(Effect.action(FetchItems))
          else noChange
        case FetchItems =>
          val pageSize = 16
          val limit = math.min(pageSize, value.total - value.items.length)
          if (!value.loading && limit > 0) {
            updated(value.copy(loading = true),
              Effect {
                api.searchItems(
                  name = value.params.name,
                  types = value.params.typeFilters,
                  effects = value.params.effectFilters,
                  rarities = value.params.rarities,
                  minLevel = Some(value.params.minLevel),
                  maxLevel = Some(value.params.maxLevel),
                  offset = value.items.length,
                  limit = limit,
                  sortBy = None,
                  sortOrd = None,
                  withBeta = true,
                  onlyBestCraft = value.params.onlyBestCraft
                ).map(resp => ItemsLoaded(resp.items, resp.count))
              })
          } else noChange
        case ItemsLoaded(results, total) =>
          val items = results.map { i =>
            ItemWithCachedEffects(i,
              EffectList.renderList(context)(i.equipEffects, SlotState.initializedLevel(i), mod = Some(Theme.itemEffects)))
          }
          updated(value.copy(items = value.items ++ items, loading = false, total = total))
      }
    }

  val ringSelectHandler: ActionHandler[BuilderState, RingSelectState] =
    new ActionHandler(zoomTo(_.ringSelect)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetRingSelect(value) => updated(RingSelectState(value))
      }
    }

  val elementPrioritiesHandler: ActionHandler[BuilderState, ElementPriorityState] =
    new ActionHandler(zoomTo(_.equipmentSearch.elementPriorities)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetElementPriorities(values) => updated(ElementPriorityState(values))
      }
    }

  val equipmentToastHandler: ActionHandler[BuilderState, EquipmentToastState] =
    new ActionHandler(zoomTo(_.equipmentToast)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetEquipmentToast(text, false) if value.toastText.contains(text) =>
          updated(EquipmentToastState(None))
        case SetEquipmentToast(text, _) =>
          updated(EquipmentToastState(Some(text)))
      }
    }

  val spellToastHandler: ActionHandler[BuilderState, SpellToastState] =
    new ActionHandler(zoomTo(_.spellToast)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetSpellToast(text, false) if value.toastText.contains(text) =>
          updated(SpellToastState(None))
        case SetSpellToast(text, _) =>
          updated(SpellToastState(Some(text)))
      }
    }

  val levelHandler: ActionHandler[BuilderState, CharacterLevelState] =
    new ActionHandler(zoomTo(_.character.level)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetLevel(value) =>
          updated(
            CharacterLevelState(value),
            Effect.action(UpdateSkills(const(Skills()))) >>
              Effect.action(UpdateSpells(const(SpellsState())))
          )
      }
    }

  val classHandler: ActionHandler[BuilderState, ClassState] =
    new ActionHandler(zoomTo(_.character.`class`)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetClass(value) =>
          updated(
            ClassState(value),
            Effect.action(LoadClassData(value)) >>
              Effect.action(UpdateSkills(const(Skills()))) >>
              Effect.action(UpdateSpells(const(SpellsState())))
          )
      }
    }

  val buildCodeDialogHandler: ActionHandler[BuilderState, BuildCodeDialogState] =
    new ActionHandler(zoomTo(_.buildCodeDialog)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetBuildCodeDialog(value) => updated(BuildCodeDialogState(value))
      }
    }

  val appearanceHandler: ActionHandler[BuilderState, AppearanceState] =
    new ActionHandler(zoomTo(_.character.appearance)) {
      override protected def handle: HandleFunction[BuilderState] = {
        case SetAppearance(value, _) => updated(value)
        case MoveStanceRight | MoveStanceLeft => noChange
      }
    }

  val equipmentSlotSetter: PartialFunction[ItemType, Option[SlotState] => Equipment[SlotState] => Equipment[SlotState]] = {
    case ItemType.Helmet => Equipment.headgear.set
    case ItemType.Cape => Equipment.cape.set
    case ItemType.Amulet => Equipment.amulet.set
    case ItemType.Epaulettes => Equipment.epaulettes.set
    case ItemType.Breastplate => Equipment.breastplate.set
    case ItemType.Belt => Equipment.belt.set
    case ItemType.Boots => Equipment.boots.set
    case ItemType.Pet => Equipment.pet.set
    case ItemType.Costume => Equipment.costume.set
    case ItemType.Emblem => Equipment.insignia.set
    case ItemType.Mount => Equipment.mount.set
    case ItemType.Axe | ItemType.Bow | ItemType.Hammer | ItemType.Shovel | ItemType.TwoHandStaff |
         ItemType.TwoHandSword | ItemType.Cards | ItemType.Hand | ItemType.OneHandStaff | ItemType.OneHandSword |
         ItemType.Wand => Weapons.setter(true)
    case ItemType.Dagger | ItemType.Shield => Weapons.setter(false)
  }
}

final case class UpdateCustomCharacteristics(modify: CustomCharacteristics => CustomCharacteristics) extends Action
final case class UpdateSkills(modify: Skills => Skills) extends Action
final case class UpdateSpells(modify: SpellsState => SpellsState) extends Action
final case class UpdateEquipment(modify: Equipment[SlotState] => Equipment[SlotState]) extends Action
final case class SetEquipment(value: Equipment[SlotState]) extends Action
case object RecalculateCharacteristics extends Action
final case class SetCharacteristics(characteristics: Characteristics) extends Action
final case class SetView(index: Int) extends Action
final case class SetSideMenu(open: Boolean, silent: Boolean) extends Action
final case class SetItemSearchParams(parameters: SearchParameters) extends Action
final case class SetEquipmentToast(value: String, visible: Boolean) extends Action
final case class SetSpellToast(value: String, visible: Boolean) extends Action
final case class SetRingSelect(value: Option[SlotState]) extends Action
final case class ItemClicked(slot: SlotState) extends Action
final case class SpellUsed(spell: Spell, lens: SpellLens, added: Boolean) extends Action
final case class SetLevel(level: Int) extends Action
final case class SetClass(`class`: CharacterClass) extends Action
final case class SetElementPriorities(values: List[Element]) extends Action
case object FetchItems extends Action
case object InitializeItemSearch extends Action
final case class ItemsLoaded(results: Seq[Item], total: Int) extends Action
final case class LoadClassData(`class`: CharacterClass) extends Action
final case class ClassDataLoaded(breed: CharacterBreed, fromBuild: Boolean) extends Action
final case class ClassLoadComplete(appearance: Option[AppearanceState] = None) extends Action
final case class SetBuildCodeDialog(state: Pot[BuildResponse]) extends Action
case object ShareBuild extends Action
final case class LoadBuild(code: String) extends Action
final case class BuildLoaded(breed: CharacterBreed, build: Build) extends Action
final case class SetCharacterState(state: CharacterState, sideEffect: diode.Effect) extends Action
final case class SetAppearance(value: AppearanceState, silent: Boolean) extends Action
case object MoveStanceRight extends Action
case object MoveStanceLeft extends Action
