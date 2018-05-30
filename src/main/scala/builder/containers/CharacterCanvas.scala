package builder.containers

import builder._
import builder.raw.onsen.Icon
import builder.state._
import cats.implicits._
import diode.react.ModelProxy
import diode.{ActionProcessor, ActionResult, Dispatcher}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import org.bitbucket.wakfuthesaurus.renderer.transform.TransformRenderingProgramDefinition
import org.bitbucket.wakfuthesaurus.renderer.transform.Transformation._
import org.bitbucket.wakfuthesaurus.renderer
import org.bitbucket.wakfuthesaurus.renderer._
import org.bitbucket.wakfuthesaurus.shared.data._
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.{Element, WebGLRenderingContext}
import scalacss.ScalaCssReact._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CharacterCanvas {
  final case class Props(
    context: Context,
    `class`: CharacterClass,
    appearance: AppearanceState,
    equipment: Equipment[SlotState]
  )

  final case class AnimationState(
    base: AnimationSet,
    hair: Option[AnimationSet],
    body: Option[AnimationSet]
  )

  final case class CachedEquipmentPart(
    id: Int,
    set: AnimationSet,
    partsToHide: Seq[String]
  )

  private var ctx: RenderingContext = _
  private var program: RenderingProgram = _
  private var scene: Scene = _
  private var animation: AnimationState = _
  private var stance: StaticActorStance = StaticActorStance.Front
  private var cachedEquipmentParts: List[CachedEquipmentPart] = List()

  MainCircuit.addProcessor(Processor)

  private def initialize(element: Element): Callback = Callback {
    val gl = element.asInstanceOf[Canvas]
      .getContext("webgl")
      .asInstanceOf[WebGLRenderingContext]
    this.ctx = new RenderingContext(gl)
    this.program = this.ctx.loadRenderingProgram(new TransformRenderingProgramDefinition(ctx)).right.get
    this.scene = new Scene(ctx.gl)
    scene.play()
  }

  def getPetPosition: (TransformationFD, Boolean) =
    stance match {
      case StaticActorStance.Front ⇒ (absoluteTranslate(0.6f, -0.73f), true)
      case StaticActorStance.LeftFront ⇒
        (absoluteTranslate(0.5f, -0.86f), true)
      case StaticActorStance.LeftProfile ⇒ (absoluteTranslate(0, -0.92f), true)
      case StaticActorStance.LeftBack ⇒
        (absoluteTranslate(-0.5f, -0.86f), true)
      case StaticActorStance.Back ⇒ (absoluteTranslate(-0.6f, -0.73f), true)
      case StaticActorStance.RightBack ⇒
        (absoluteTranslate(-0.5f, -0.52f), false)
      case StaticActorStance.RightProfile ⇒
        (absoluteTranslate(0, -0.46f), false)
      case StaticActorStance.RightFront ⇒
        (absoluteTranslate(0.5f, -0.52f), false)
    }

  def refresh(): Unit = {
    scene.clear()

    val playerAnimation: FrameActorAnimation = animation.base
      .loadFrameActorAnimation(stance.animation, stance.flipped, renderer.uoption.USome(absoluteTranslate(0, -0.7f)))
      .get

    val partsToHide = cachedEquipmentParts.flatMap(_.partsToHide)
    val elements = cachedEquipmentParts.map(_.set) ::: List(animation.hair, animation.body).flatten
    playerAnimation.setElements(elements, partsToHide)

//    val (ts, back) = getPetPosition

//    val petAnimation = pet.flatMap(
//      _.set.loadFrameAnimation(stance.animation, stance.flipped, USome(ts)))

//    if (back) {
//      scene.addAnimation(playerAnimation, program)
//      if (petAnimation.isDefined)
//        scene.addAnimation(petAnimation.get, program)
//    } else {
//      if (petAnimation.isDefined)
//        scene.addAnimation(petAnimation.get, program)
      scene.addAnimation(playerAnimation, program)
//    }
  }

  private[this] def colorToTransform(c: Color): TransformationFD =
    colorMultiply(c.red * 1.25f, c.green * 1.25f, c.blue * 1.25f, c.alpha)

  private[this] def createTransformMap(
    colors: AppearanceState): mutable.Map[Int, TransformationFD] = {
    val tuples = Seq(
      1 -> colors.skinColor,
      2 -> colors.hairColor,
      3 -> colors.guildColor1,
      4 -> colors.guildColor2,
      8 -> colors.pupilColor
    ).map { case (k, v) => k -> colorToTransform(v) }
    mutable.Map(tuples: _*)
  }

  def loadCharacter(clazz: CharacterClass, appearance: AppearanceState): Future[AnimationSet] = {
    import MainCircuit._
    val gfxId = builder.util.common.getPlayerGfx(clazz, appearance.sex)
    (api.getPlayerAnm(gfxId), api.getPlayerAtlas(gfxId)).mapN { (anm, atlas) =>
      new AnimationSet(anm, ctx.loadTexture(atlas), false, createTransformMap(appearance))
    }
  }

  def loadHair(clazz: CharacterClass, appearance: AppearanceState): Future[Option[AnimationSet]] =
    if ((clazz == CharacterClass.Panda && appearance.hairIdx == 0) ||
      (clazz == CharacterClass.Rogue && appearance.sex == CharacterSex.Male && appearance.hairIdx == 0))
      Future.successful(None)
    else {
      import MainCircuit._

      val gfxId = builder.util.common.getHairGfx(clazz, appearance.sex, appearance.hairIdx)
      (api.getItemAnm(gfxId), api.getItemAtlas(gfxId)).mapN { (anm, atlas) =>
        Some(new AnimationSet(anm, ctx.loadTexture(atlas), partsFilter = renderer.util.CustomHairParts.contains))
      }
    }

  def loadBody(clazz: CharacterClass, appearance: AppearanceState): Future[Option[AnimationSet]] =
    if (clazz == CharacterClass.Fog) Future.successful(None)
    else {
      import MainCircuit._

      val gfxId = builder.util.common.getDressStyleGfx(clazz, appearance.sex, appearance.bodyIdx)
      (api.getItemAnm(gfxId), api.getItemAtlas(gfxId)).mapN { (anm, atlas) =>
        Some(new AnimationSet(anm, ctx.loadTexture(atlas), partsFilter = renderer.util.CustomBodyParts.contains))
      }
    }

  def loadGearPart(gfx: Int, partsToHide: Seq[String]): Future[CachedEquipmentPart] =
    cachedEquipmentParts.find(_.id == gfx) match {
      case Some(anim) => Future.successful(anim)
      case None =>
        import MainCircuit._

        (api.getItemAnm(gfx.toString), api.getItemAtlas(gfx.toString)).mapN { (anm, atlas) =>
          CachedEquipmentPart(gfx, new AnimationSet(anm, ctx.loadTexture(atlas)), partsToHide)
        }
    }

  def getPartsToHide(actions: List[ApplyHmiAction], test: Seq[String] => Boolean): List[String] =
    actions.collect {
      case ApplyHmiAction(conditions, SkinPartVisibilityAction(visible, parts)) if test(conditions) =>
        if (!visible) parts
        else Nil
    }.flatten

  def oppositeSexCondition(sex: CharacterSex): String = sex match {
    case CharacterSex.Male => "IsSex(\"female\")"
    case CharacterSex.Female => "IsSex(\"male\")"
  }
  
  def loadCostume(sex: CharacterSex)(part: Option[SlotState]): List[Future[CachedEquipmentPart]] =
    part.map { slot =>
      val negation = oppositeSexCondition(sex)
      val list = slot.item.hmiActions.toList
      val partsToHide =
        getPartsToHide(list, !_.contains(negation))
      list.collectFirst {
        case ApplyHmiAction(conditions, CostumeAction(_, _, appearances)) if !conditions.exists(_.contains(negation)) ⇒
          appearances.toList.filter(_.id.startsWith("equipment/")).map {
            appearance ⇒
              appearance.id.substring(appearance.id.indexOf('/') + 1).toInt
          }
      }
        .getOrElse(Nil).map(loadGearPart(_, partsToHide))
    }.getOrElse(Nil)


  def loadEquipment(sex: CharacterSex, equipment: Equipment[SlotState]): Future[List[CachedEquipmentPart]] = {
    val negation = oppositeSexCondition(sex)

    val costumeSlotParts = loadCostume(sex)(equipment.costume)
    val costumeParts =
      if (costumeSlotParts.nonEmpty) costumeSlotParts
      else loadCostume(sex)(equipment.insignia)

    val equipmentParts =
      costumeParts :::
        List(
          equipment.headgear,
          equipment.epaulettes,
          equipment.breastplate,
          equipment.cape,
          equipment.boots
        ).collect { case Some(v) => v }
          .map { gear ⇒
            val gfx =
              sex match {
                case CharacterSex.Male => gear.item.gfxId
                case CharacterSex.Female => gear.item.femaleGfxId
              }
            val partsToHide =
              getPartsToHide(gear.item.hmiActions.toList, !_.contains(negation))
            loadGearPart(gfx, partsToHide)
          }

    equipmentParts.map(_.attempt)
      .sequence
      .map(_.collect { case Right(v) => v })
  }

  object Processor extends ActionProcessor[BuilderState] {
    override def process(
      dispatch: Dispatcher,
      action: Any,
      next: Any => ActionResult[BuilderState],
      currentModel: BuilderState
    ): ActionResult[BuilderState] = {
      def reloadCharacter(appearance: AppearanceState, clazz: CharacterClass): Future[Unit] =
        for {
          char <- loadCharacter(clazz, appearance)
          body <- loadBody(clazz, appearance)
          hair <- loadHair(clazz, appearance)
        } yield {
          animation = AnimationState(char, hair, body)
        }

      def reloadEquipment(sex: CharacterSex, equipment: Equipment[SlotState]): Future[Unit] =
        loadEquipment(sex, equipment).map { parts =>
          cachedEquipmentParts = parts
        }

      action match {
        case ClassLoadComplete(Some(appearance)) =>
          reloadCharacter(appearance, currentModel.character.`class`.`class`)
            .foreach(_ => refresh())
        case ClassLoadComplete(None) =>
          reloadCharacter(currentModel.character.appearance, currentModel.character.`class`.`class`)
            .flatMap(_ => reloadEquipment(currentModel.character.appearance.sex, currentModel.character.equipment))
            .foreach(_ => refresh())
        case SetEquipment(equipment) =>
          reloadEquipment(currentModel.character.appearance.sex, equipment)
            .foreach(_ => refresh())
        case SetAppearance(appearance, false) =>
          if (appearance.sex != currentModel.character.appearance.sex ||
            appearance.bodyIdx != currentModel.character.appearance.bodyIdx ||
            appearance.hairIdx != currentModel.character.appearance.hairIdx) {
            reloadCharacter(appearance, currentModel.character.`class`.`class`)
              .flatMap(_ => reloadEquipment(appearance.sex, currentModel.character.equipment))
              .foreach(_ => refresh())
          } else {
            animation.base.colorTransformMap.clear()
            animation.base.colorTransformMap ++= createTransformMap(appearance)
          }
        case MoveStanceLeft =>
          stance = StaticActorStance.left(stance)
          refresh()
        case MoveStanceRight =>
          stance = StaticActorStance.right(stance)
          refresh()
        case _ =>
      }
      next(action)
    }
  }

  def apply(): VdomElement = MainCircuit.wrap(m => m)(component(_))

  private val component = ScalaComponent.builder[ModelProxy[_]]("CharacterCanvas")
    .render_P { model => VdomArray(
      <.canvas(^.width := "120px", ^.height := "160px"),
      <.div(Theme.previewControls,
        <.div(^.onClick --> model.dispatchCB(MoveStanceRight), Icon(icon = "md-rotate-right")),
        <.div(^.onClick --> model.dispatchCB(MoveStanceLeft), Icon(icon = "md-rotate-left"))
      )
    )
    }.componentDidMount($ => initialize($.getDOMNode.asElement))
    .shouldComponentUpdateConst(false)
    .build
}
