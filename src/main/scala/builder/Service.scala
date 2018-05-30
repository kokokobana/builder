package builder

import builder.Service._
import builder.state.Weapons.{Bare, OneHanded, TwoHanded}
import builder.state._
import cats.data.{Ior, OptionT}
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.bitbucket.wakfuthesaurus.shared.anm._
import org.bitbucket.wakfuthesaurus.shared.data._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html.Image

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final class Service(val url: String, bucket: String, lang: String = "en") {
  import io.circe.generic.auto._

  def get[R: Decoder](endpoint: String): Future[R] =
    Ajax.get(url + endpoint).flatMap { xhr =>
      Future.fromTry(decode[R](xhr.responseText).toTry)
    }

  def post[R: Decoder, D: Encoder](
    endpoint: String,
    data: D
  ): Future[R] =
    Ajax.post(url + endpoint, data = data.asJson.noSpaces).flatMap { xhr =>
      Future.fromTry(decode[R](xhr.responseText).toTry)
    }

  def getItem(id: Int): Future[Item] =
    get[Item](s"/api/items/$id?lang=$lang")

  def getState(id: Int): Future[State] =
    get[State](s"/api/states/$id?lang=$lang")

  def getBreed(id: Int): Future[CharacterBreed] =
    get[CharacterBreed](s"/api/breeds/$id?lang=$lang")

  def getPet(refId: Int): Future[Pet] =
    get[Pet](s"/api/pets/$refId")

  def getBuild(code: String): Future[BuildResponse] =
    get[BuildResponse](s"/api/builds/$code")(Service.BuildResponse.decoder)

  def saveBuild(build: Build): Future[BuildResponse] =
    post[BuildResponse, Build](s"/api/builds", build)(Service.BuildResponse.decoder, Service.Build.encoder)

  def searchItems(
    name: Option[String],
    types: Seq[ItemFilter],
    effects: Seq[ItemEffectFilter],
    rarities: Seq[ItemRarity],
    minLevel: Option[Int],
    maxLevel: Option[Int],
    offset: Int,
    limit: Int,
    sortBy: Option[String],
    sortOrd: Option[String],
    withBeta: Boolean,
    onlyBestCraft: Boolean
  ): Future[PaginationResponse] = {
    val nameParam = name.map(n => "&name=" + n)
    val typeParams = types.foldLeft("") { (acc, at) =>
      acc + s"&types=${at.entryName}"
    }
    val bonusParams = effects.foldLeft("") { (acc, at) =>
      acc + s"&effects=${at.entryName}"
    }
    val rarityParams = rarities.foldLeft("") { (acc, at) =>
      acc + s"&rarities=${at.value}"
    }
    val endpoint = s"/api/items/search?offset=$offset&limit=$limit" +
      nameParam.getOrElse("") +
      typeParams +
      bonusParams +
      rarityParams +
      minLevel.fold("")(str => s"&minLvl=$str") +
      maxLevel.fold("")(str => s"&maxLvl=$str") +
      sortBy.fold("")(str ⇒ s"&sortBy=$str") +
      sortOrd.fold("")(str ⇒ s"&sortOrd=$str") +
      s"&withBeta=$withBeta&onlyBestCraft=$onlyBestCraft&lang=$lang"

    get[PaginationResponse](endpoint)
  }

  def getItemAnm(id: String): Future[AnimationDefinition] =
    get[AnimationDefinition](s"/api/anm/items/$id")

  def getPetAnm(id: String): Future[AnimationDefinition] =
    get[AnimationDefinition](s"/api/anm/pets/$id")

  def getPlayerAnm(id: String): Future[AnimationDefinition] =
    get[AnimationDefinition](s"/api/anm/breeds/$id")

  def getItemAtlas(id: String): Future[Image] =
    util.common.loadImage(s"$bucket/atlas/equipments/${id}_0.png")

  def getPetAtlas(id: String): Future[Image] =
    util.common.loadImage(s"$bucket/atlas/pets/${id}_0.png")

  def getPlayerAtlas(id: String): Future[Image] =
    util.common.loadImage(s"$bucket/atlas/players/${id}_0.png")
}

object Service {
  import io.circe.generic.semiauto._
  import cats.implicits._

  final case class PaginationResponse(items: Seq[Item], count: Int)

  final case class BuildResponse(code: String, build: Build)

  object BuildResponse {
    implicit val decoder: Decoder[BuildResponse] = deriveDecoder[BuildResponse]
  }

  final case class Build(
    level: Int,
    sex: CharacterSex,
    `class`: CharacterClass,
    custom: CustomCharacteristics,
    skills: Skills,
    actives: List[Option[Int]],
    passives: List[Option[Int]],
    equipment: Equipment[Slot],
    appearance: AppearanceState = AppearanceState()
  )

  object Build {
    implicit val characterSexEncoder: Encoder[CharacterSex] =
      Encoder.encodeInt.contramap[CharacterSex](_.value)
    implicit val characterSexDecoder: Decoder[CharacterSex] =
      Decoder.decodeInt.emap[CharacterSex] { i =>
        CharacterSex.withValueOpt(i).toRight(s"Invalid character sex enum value: $i")
      }

    implicit val characterClassEncoder: Encoder[CharacterClass] =
      Encoder.encodeInt.contramap[CharacterClass](_.value)
    implicit val characterClassDecoder: Decoder[CharacterClass] =
      Decoder.decodeInt.emap[CharacterClass] { i =>
        CharacterClass.withValueOpt(i).toRight(s"Invalid character class enum value: $i")
      }

    implicit val customCharacteristicsEncoder: Encoder[CustomCharacteristics] =
      deriveEncoder[CustomCharacteristics]
    implicit val customCharacteristicsDecoder: Decoder[CustomCharacteristics] =
      deriveDecoder[CustomCharacteristics]

    implicit val colorEncoder: Encoder[Color] =
      deriveEncoder[Color]
    implicit val colorDecoder: Decoder[Color] =
      deriveDecoder[Color]

    implicit val appearanceEncoder: Encoder[AppearanceState] =
      deriveEncoder[AppearanceState]
    implicit val appearanceDecoder: Decoder[AppearanceState] =
      deriveDecoder[AppearanceState]

    implicit val majorEncoder: Encoder[Skills.Major] = deriveEncoder[Skills.Major]
    implicit val majorDecoder: Decoder[Skills.Major] = deriveDecoder[Skills.Major]
    implicit val agilityEncoder: Encoder[Skills.Agility] = deriveEncoder[Skills.Agility]
    implicit val agilityDecoder: Decoder[Skills.Agility] = deriveDecoder[Skills.Agility]
    implicit val chanceEncoder: Encoder[Skills.Chance] = deriveEncoder[Skills.Chance]
    implicit val chanceDecoder: Decoder[Skills.Chance] = deriveDecoder[Skills.Chance]
    implicit val intelligenceEncoder: Encoder[Skills.Intelligence] = deriveEncoder[Skills.Intelligence]
    implicit val intelligenceDecoder: Decoder[Skills.Intelligence] = deriveDecoder[Skills.Intelligence]
    implicit val strengthEncoder: Encoder[Skills.Strength] = deriveEncoder[Skills.Strength]
    implicit val strengthDecoder: Decoder[Skills.Strength] = deriveDecoder[Skills.Strength]
    implicit val skillsEncoder: Encoder[Skills] = deriveEncoder[Skills]
    implicit val skillsDecoder: Decoder[Skills] = deriveDecoder[Skills]

    implicit val weaponsEncoder: Encoder[Weapons[Slot]] =
      Encoder.instance {
        case Weapons.Bare => Json.obj()
        case Weapons.TwoHanded(state) => Json.obj("both" -> state.asJson)
        case Weapons.OneHanded(state) => Json.obj("left" -> state.left.asJson, "right" -> state.right.asJson)
      }
    implicit val weaponsDecoder: Decoder[Weapons[Slot]] =
      Decoder.instance[Weapons[Slot]] { c =>
        c.downField("both").as[Slot].map(Weapons.TwoHanded(_))
      } or Decoder.instance[Weapons[Slot]] { c =>
        (c.downField("left").as[Option[Slot]], c.downField("right").as[Option[Slot]])
          .mapN(Ior.fromOptions)
          .flatMap(_.toRight(DecodingFailure("Failed to decode weapons", Nil)))
          .map(Weapons.OneHanded(_))
      } or Decoder.const[Weapons[Slot]](Weapons.Bare)

    implicit val equipmentEncoder: Encoder[Equipment[Slot]] = deriveEncoder[Equipment[Slot]]
    implicit val equipmentDecoder: Decoder[Equipment[Slot]] = deriveDecoder[Equipment[Slot]]

    implicit val encoder: Encoder[Build] = deriveEncoder[Build]
    implicit val decoder: Decoder[Build] = deriveDecoder[Build]

    def toState(service: Service)(breed: CharacterBreed, build: Build): Future[CharacterState] =
      toEquipment(service)(build.equipment).map { eq =>
        CharacterState(
          level = CharacterLevelState(build.level),
          `class` = ClassState(build.`class`),
          customCharacteristics = build.custom,
          skills = build.skills,
          spells = SpellsState(
            actives = build.actives.map(_.flatMap { i =>
              breed.spells.find(_.id == i).orElse(breed.spells.find(_.id == i))
            }),
            passives = build.passives.map(_.flatMap { i =>
              breed.passive.find(_.id == i)
            })
          ),
          equipment = eq,
          appearance = build.appearance
        )
      }

    def toEquipment(service: Service)(equipment: Equipment[Slot]): Future[Equipment[SlotState]] = {
      def getSlot(maybeSlot: Option[Slot]): Future[Option[SlotState]] =
        OptionT.fromOption[Future](maybeSlot).semiflatMap { slot =>
          service.getItem(slot.itemId).map { item =>
            SlotState(item, slot.level, slot.variableElementMastery, slot.variableElementResist, slot.runes)
          }
        }.value
      for {
        headgear <- getSlot(equipment.headgear)
        cape <- getSlot(equipment.cape)
        amulet <- getSlot(equipment.amulet)
        epaulettes <- getSlot(equipment.epaulettes)
        breastplate <- getSlot(equipment.breastplate)
        belt <- getSlot(equipment.belt)
        leftRing <- getSlot(equipment.leftRing)
        rightRing <- getSlot(equipment.rightRing)
        boots <- getSlot(equipment.boots)
        pet <- getSlot(equipment.pet)
        costume <- getSlot(equipment.costume)
        weapons <- equipment.weapon match {
          case Weapons.TwoHanded(slot) =>
            getSlot(Some(slot)).map(_.fold[Weapons[SlotState]](Bare)(TwoHanded[SlotState]))
          case Weapons.OneHanded(state) =>
            (getSlot(state.left), getSlot(state.right))
              .mapN(Ior.fromOptions)
              .map(_.fold[Weapons[SlotState]](Bare)(OneHanded[SlotState]))
          case Weapons.Bare => Future.successful(Bare)
        }
        insignia <- getSlot(equipment.insignia)
        mount <- getSlot(equipment.mount)
      } yield Equipment(headgear, cape, amulet, epaulettes, breastplate, belt, leftRing, rightRing, boots, pet,
        costume, weapons, insignia, mount)
    }

    def fromState(character: CharacterState): Build =
      Build(
        character.level.level,
        character.appearance.sex,
        character.`class`.`class`,
        character.customCharacteristics,
        character.skills,
        character.spells.actives.map(_.map(_.id)),
        character.spells.passives.map(_.map(_.id)),
        fromEquipment(character.equipment),
        character.appearance
      )

    def fromEquipment(equipment: Equipment[SlotState]): Equipment[Slot] =
      Equipment(
        headgear = equipment.headgear.map(Slot.fromState),
        cape = equipment.cape.map(Slot.fromState),
        amulet = equipment.amulet.map(Slot.fromState),
        epaulettes = equipment.epaulettes.map(Slot.fromState),
        breastplate = equipment.breastplate.map(Slot.fromState),
        belt = equipment.belt.map(Slot.fromState),
        leftRing = equipment.leftRing.map(Slot.fromState),
        rightRing = equipment.rightRing.map(Slot.fromState),
        boots = equipment.boots.map(Slot.fromState),
        pet = equipment.pet.map(Slot.fromState),
        costume = equipment.costume.map(Slot.fromState),
        weapon = equipment.weapon.map(Slot.fromState),
        insignia = equipment.insignia.map(Slot.fromState),
        mount = equipment.mount.map(Slot.fromState)
      )
  }

  final case class Slot(
    itemId: Int,
    level: Int,
    variableElementMastery: List[Element],
    variableElementResist: List[Element],
    runes: List[Option[Rune]]
  )

  object Slot {
    implicit val runeTypeEncoder: Encoder[RuneType] =
      Encoder.encodeString.contramap[RuneType](_.entryName)
    implicit val runeTypeDecoder: Decoder[RuneType] =
      Decoder.decodeString.emap { value =>
        RuneType.withNameOption(value).toRight(s"Invalid rune type enum value: $value")
      }
    implicit val elementEncoder: Encoder[Element] =
      Encoder.encodeInt.contramap[Element](_.value)
    implicit val elementDecoder: Decoder[Element] =
      Decoder.decodeInt.emap { value =>
        Element.withValueOpt(value).toRight(s"Invalid element enum value: $value")
      }

    implicit val runeEncoder: Encoder[Rune] = deriveEncoder[Rune]
    implicit val runeDecoder: Decoder[Rune] = deriveDecoder[Rune]

    implicit val encoder: Encoder[Slot] = deriveEncoder[Slot]
    implicit val decoder: Decoder[Slot] = deriveDecoder[Slot]

    def fromState(state: SlotState): Slot =
      Slot(state.item.id, state.level, state.variableElementMastery, state.variableElementResist, state.runes)
  }
}
