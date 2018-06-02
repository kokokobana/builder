package builder

import builder.CssSettings._
import scalacss.internal.StyleA
import scalacss.internal.mutable.StyleSheet

import scala.language.postfixOps

object Theme extends StyleSheet.Inline {

  import dsl._

  val component = style(
    fontSize(14 px)
  )

  val desktopBar = style(
    display.flex,
    flexDirection.row,
    alignItems.center,
    padding(0 px, 8 px)
  )

  val desktopBarItem = style(
    display.flex,
    flexDirection.column,
    alignItems.center,
    marginRight(8 px),
    fontSize(0.82 em)
  )

  val classSelectItem = desktopBarItem + style(
  )

  val levelInputItem = desktopBarItem + style(
    width(60 px),
    unsafeChild("input")(
      height(20 px)
    )
  )

  val elementPriorityListItem = desktopBarItem + style(
    width(60 px),
    borderLeft(1 px, solid, c"#d7d7e1"),
    borderRight(1 px, solid, c"#d7d7e1")
  )

  val characterPreview = style(
    backgroundColor(c"#efeff4"),
    borderLeft(1 px, solid, c"#d7d7e1"),
    display.flex,
    flexDirection.column,
    alignItems.center,
    paddingTop(140 px)
  )

  val previewControls = style(
    display.flex,
    flexDirection.row,
    fontSize(28 px),
    width(80 px),
    justifyContent.spaceBetween
  )

  val equipmentSlotContainer = style(
    width(40 px),
    height(40 px),
    position.relative,
    media.minWidth(375 px)(
      width(46 px),
      height(46 px),
    )
  )

  val equipmentSlotIcon = style(
    width(100 %%),
    height(100 %%),
    marginBottom(1 px)
  )

  val equipmentItemIcon = style(
    position.absolute,
    width(40 px),
    height(40 px),
    top(50 %%),
    left(50 %%),
    transform := "translate(-50%,-50%)"
  )

  val equipmentItemRemove = style(
    position.absolute,
    top(-2 px),
    right(2 px),
    cursor.pointer
  )

  val deckContainer = style(
    maxWidth(340 px),
    margin.auto
  )

  val deckHeader = style(
    padding(1 px),
    margin(2 px),
    color.white,
    borderRadius(4 px, 4 px, 0 px, 0 px),
    boxShadow := "0 0 2px 0 rgba(0,0,0,.2)",
    background := "linear-gradient(to bottom,#516169 0,#748c94 100%)",
    textAlign.center,
    fontWeight._700,
    fontSize(0.9 em)
  )

  val deckRow = style(
    display.flex,
    flexDirection.row,
    justifyContent.center,
    marginTop(2 px)
  )

  val deckEntry = style(
    width(46 px),
    height(46 px),
    marginRight(1 px),
    position.relative
  )

  val deckSlot = style(
    width(100 %%),
    height(100 %%)
  )

  val deckSpell = style(
    position.absolute,
    width(90 %%),
    height(90 %%),
    top(50 %%),
    left(50 %%),
    transform := "translate(-50%,-50%)"
  )

  val deckCode = style(
    width(100 %%)
  )

  val spellRow = style(
    display.flex,
    flexDirection.row,
    flexWrap.wrap,
    justifyContent.center
  )

  val spellEntryContainer = style(
    position.relative,
    width(48 px),
    height(48 px),
    margin(1 px),
    backgroundColor.white,
    border(1 px, solid, gray),
    borderRadius(4 px)
  )

  val spellEntry = style(
    position.absolute,
    width(94 %%),
    height(94 %%),
    top(50 %%),
    left(50 %%),
    transform := "translate(-50%,-50%)"
  )

  val inactiveSpellEntry = spellEntry + style(
    filter := "grayscale(100%)"
  )

  val toolbarHeader = style(
    display.flex,
    flexDirection.row,
    alignItems.center
  )

  val toolbarHeaderIcon = style(
    width(38 px),
    height(38 px),
    marginRight(10 px)
  )

  val spellInfoRow = style(
    display.flex,
    flexDirection.row,
    flexWrap.wrap,
    justifyContent.center,
    alignItems.center
  )

  val spellInfoEntry = style(
    display.flex,
    flexDirection.row,
    alignItems.center,
    marginRight(6 px)
  )

  val spellInfoIcon = style(
    marginRight(4 px)
  )

  val spellUseContainer = style(
    position.absolute,
    top(4 px),
    right(4 px)
  )

  val spellSegment = style(
    display.flex,
    flexDirection.column,
    alignItems.center,
    marginTop(6 px),
    unsafeChild(".segment")(
      width(240 px)
    )
  )

  val spellTabContainer = style(
    marginTop(6 px),
    width(100 %%)
  )

  val spellEffectsContainer = style(
    borderRadius(4 px),
    backgroundColor(c"#f5f5f5"),
    border(1 px, solid, c"#cfcfcf"),
    margin(3 px),
    padding(3 px)
  )

  val spellEffectsHeader = style(
    backgroundColor(c"#dedede"),
    textAlign.center,
    fontSize(0.94 em),
    padding(1 px)
  )

  val spellEffects = style(
    fontSize(0.88 em),
    width(100 %%),
    lineHeight(1.4 em),
    backgroundColor(c"#c2c2c2"),
    unsafeChild("> div")(
      paddingLeft(6 px),
    )
  )

  val spellRequirements = style(
    marginTop(6 px),
    fontSize(0.92 em)
  )

  val noRequirements = style(
    textAlign.center,
    marginTop(6 px),
    fontSize(0.92 em)
  )

  val multiselect = style(
    margin(12 px, 40 px)
  )

  val itemsLevelRange = style(
    display.flex,
    alignItems.center,
    justifyContent.center,
    margin(0 px, 40 px),
    unsafeChild("input")(
      maxWidth(60 px),
      margin(0 px, 12 px)
    )
  )

  val switchRow = style(
    display.flex,
    flexDirection.row,
    justifyContent.center,
    alignItems.center
  )

  val label = style(
    marginLeft(12 px)
  )

  val itemRow = style(
    display.flex,
    flexDirection.row,
    flexWrap.wrap,
    alignItems.center,
    backgroundColor.white,
    color(c"#747474"),
    borderBottom(1 px, solid, c"#EEEEEE")
  )

  val itemIcon = style(
    padding(2 px),
    marginLeft(4 px),
    flexGrow(0),
  )

  val colorOutline: String => StyleA = styleF {
    Domain.ofValues(
      "red",
      "#ffff80",
      "#ffa6d6",
      "#94f0f5",
      "#cf73f2",
      "#f90",
      "#00bf00",
      "#a6aba1"
    )
  } { color =>
    styleS(
      filter := s"drop-shadow(1px 1px 0 $color) drop-shadow(-1px -1px 0 $color) drop-shadow(1px 1px 0 rgba(1, 1, 1, 0.5));")
  }

  val admin = colorOutline("red")
  val legendary = colorOutline("#ffff80")
  val epic = colorOutline("#ffa6d6")
  val pvp = colorOutline("#94f0f5")
  val relic = colorOutline("#cf73f2")
  val mythical = colorOutline("#f90")
  val rare = colorOutline("#00bf00")
  val common = colorOutline("#a6aba1")

  val itemName = style(
    marginLeft(4 px),
    flexGrow(1),
    fontSize(1 em),
    fontWeight.bold
  )

  val itemLevel = style(
    marginRight(4 px),
    fontSize(0.84 em),
    flexGrow(0)
  )

  val itemEffects = style(
    fontSize(0.84 em),
    width(100 %%),
    borderTop(1 px, solid, c"#F0F0F0"),
    unsafeChild("> div")(
      backgroundColor(c"#FAFAFA"),
      paddingLeft(6 px),
      &.nthChild("even")(
        backgroundColor(c"#F5F5F5"),
      )
    )
  )

  val expandedItemEffects = style(
    fontSize(0.88 em),
    width(100 %%),
    unsafeChild("> div")(
      backgroundColor(c"#FAFAFA"),
      padding(2 px, 6 px),
      &.nthChild("even")(
        backgroundColor(c"#F5F5F5"),
      )
    )
  )

  val iconWithinEffect = style(
    marginLeft(1 px),
    marginRight(1 px),
    verticalAlign.middle
  )

  // characteristics
  val characteristicTable = style(
    backgroundColor(c"#fdfdfd"),
    tableLayout.fixed,
    width(100 %%),
    borderCollapse.separate,
    borderSpacing(3 px),
    padding.`0`,
    margin.`0`,
    color.black
  )

  val characteristicHeader = style(
    textAlign.center,
    fontWeight._700,
    color.white,
    borderRadius(4 px, 4 px, 0 px, 0 px),
    background := "linear-gradient(to bottom,#516169 0,#748c94 100%)",
    boxShadow := "0 0 2px 0 rgba(0,0,0,.16);",
    padding(2 px)
  )

  val characteristicCell = style(
    padding.`0`
  )

  val characteristicContent = style(
    backgroundColor(c"#e6e6e6"),
    boxShadow := "0 0 2px 0 rgba(0,0,0,.16);",
    border(3 px, solid, c"#fff"),
    borderRadius(4 px),
    display.flex,
    flexDirection.row,
    alignItems.center,
    fontSize(0.9 em)
  )

  val characteristicValue = style(
    marginLeft.auto,
    marginRight(4 px)
  )

  val characteristicInput =
    characteristicValue + style(
      marginRight(0 px),
      unsafeChild("input")(
        height(22 px),
        fontSize(1 em)
      ),
      maxWidth(26 px),
      media.minWidth(375 px)(
        maxWidth(32 px)
      ),
      media.minWidth(425 px)(
        maxWidth(42 px)
      )
    )

  val characteristicPositive =
    characteristicValue + style(
      color.green
    )

  val characteristicNeutral =
    characteristicValue + style(
      color(c"#000")
    )

  val characteristicNegative =
    characteristicValue + style(
      color.red
    )

  val characteristicImage = style(
    margin(2 px)
  )

  val characteristicName = style(
    marginLeft(2 px)
  )

  val masteryContent =
    characteristicContent + style(

    )

  val masteryIcon = style(
    margin(0 px, 2 px)
  )

  val masteryCharacteristic = style(
    flexGrow(1),
    display.flex,
    alignItems.center
  )

  val masteryValue = style(
    marginLeft(2 px)
  )

  val mainCharacteristicName =
    characteristicName + style(
      display.none,
      media.minWidth(768 px)(
        display.block
      )
    )

  val skillGroupHeader = style(
    backgroundColor(c"#52626a"),
    padding(3 px),
    borderBottom(1 px, c"#364048", solid),
    borderTop(1 px, c"#99acb5", solid),
    borderTopLeftRadius(4 px),
    borderTopRightRadius(4 px),
    color.white,
    position.relative,
    margin(1 px)
  )

  val skillGroupTitle = style(
    fontWeight._700,
    fontSize(1 em),
    textAlign.center
  )

  val skillGroupPoints = style(
    position.absolute,
    right(4 px),
    top(4 px),
    fontSize(0.92 em)
  )

  val skillGroup = style(
    paddingLeft.`0`,
    listStyleType := "none",
    margin(4 px, 0 px, 4 px, 0 px)
  )

  val skillRow = style(
    display.flex,
    flexDirection.row,
    padding(0 px, 3 px),
    backgroundColor(c"#828e8e"),
    borderBottom(2 px, c"#556665", solid),
    borderTop(2 px, c"#aeb6b6", solid),
    borderRadius(4 px),
    alignItems.center,
    color.white,
    margin(2 px)
  )

  val skillName = style(
    marginRight.auto,
    marginLeft(6 px)
  )

  val skillRowIcon = style(
    margin(0 px, 2 px)
  )

  val skillValue = style(
    width(22 px),
    textAlign.center,
    fontWeight.bold
  )

  val maxSkillValue = skillValue +
    style(
      color(c"#ffd020")
    )

  val itemRuneSlot = style(
    margin(2 px),
    width(34 px),
    height(34 px),
    backgroundColor(c"#f7f7f7")
  )

  val defensiveRuneSlot = itemRuneSlot + style(
    border(1 px, solid, c"#05b9c3")
  )

  val offensiveRuneSlot = itemRuneSlot + style(
    border(1 px, solid, red)
  )

  val supportRuneSlot = itemRuneSlot + style(
    border(1 px, solid, c"#b0dd03")
  )

  val itemRuneIcon = style(
    width(100 %%),
    height(100 %%),
    padding(1 px)
  )

  val itemElementsSelect = style(
    display.flex,
    flexDirection.row,
    flexWrap.nowrap,
    justifyContent.spaceAround,
    margin(2 px),
    maxWidth(300 px)
  )

  val itemElementsBox = style(
    display.flex,
    alignItems.center
  )

  val itemElementsIcon = style(
    marginRight(4 px)
  )

  val itemRunes = style(
    display.flex,
    flexDirection.column,
    margin(2 px)
  )

  val itemRuneRow = style(
    display.flex,
    alignItems.center,
    fontSize(0.94 em)
  )

  val itemRuneLabel = style(
    paddingLeft(4 px)
  )

  val itemHeaderRow = style(
    display.flex,
    justifyContent.center,
    padding(2 px, 0 px),
    borderTop(1 px, solid, c"#e6e6e6"),
    borderBottom(1 px, solid, c"#e6e6e6"),
    backgroundColor(c"#f3f3f3"),
    fontSize(0.96 em)
  )

  val runePopupContent = style(
    marginTop(6 px),
    fontSize(0.96 em),
    unsafeChild("input,select")(
      fontSize(0.96 em).important
    )
  )

  val runeLevelInputContainer = style(
    display.flex,
    flexDirection.row,
    alignItems.center,
    justifyContent.center,
    marginTop(4 px),
  )

  val runeLevelInput = style(
    width(32 px),
    marginRight(6 px)
  )

  val levelInput = style(
    width(60 px)
  )

  val splitterContent = style(
    display.flex,
    flexDirection.column
  )

  val splitterRow = style(
    display.flex,
    flexDirection.row,
    flexWrap.wrap,
    alignItems.center,
    marginTop(12 px)
  )

  val splitterLabel = style(
    marginRight(8 px),
    marginLeft(6 px),
    marginBottom(6 px)
  )

  val splitterButton = style(
    marginTop(6 px),
    alignSelf.center,
    width(100 px)
  )

  val elementPriorityList = style(
    width(100 %%),
    padding.`0`,
    margin.`0`,
    listStyleType := "none",
  )

  val elementPriority = style(
    display.flex,
    flexDirection.row,
    alignItems.center,
    paddingLeft(4 px),
    backgroundColor(c"#FAFAFA"),
    &.nthChild("even")(
      backgroundColor(c"#F5F5F5"),
    )
  )

  val smallElementPriority = elementPriority + style(
    height(13 px)
  )

  val elementPriorityLabel = style(
    marginLeft(6 px)
  )

  val elementPriorityControls = style(
    marginLeft.auto,
    display.flex,
    flexDirection.row,
    justifyContent.spaceAround
  )

  val buildCodePopupContent = style(
    display.flex,
    flexDirection.column,
    alignItems.center,
    justifyContent.spaceAround,
    height(136 px),
    marginTop(10 px)
  )

  val buildCodePopupRow = style(
    display.flex,
    flexDirection.row,
    alignItems.center,
    unsafeChild("input")(
      marginRight(24 px),
      fontSize(0.88 em)
    )
  )

  val colorSelect = style(
    width(25 px),
    height(25 px),
    padding(2 px),
    border(1 px, solid, c"#bababa"),
    borderRadius(6 px),
    cursor.pointer
  )

  val colorValue = style(
    width(100 %%),
    height(100 %%),
    borderRadius(4 px)
  )

  val colorDropdownContainer = style(
    position.relative,
    display.flex,
    flexDirection.column,
    alignItems.center,
    opacity(0.96),
    marginTop(2 px)
  )

  val colorDropdown = style(
    position.absolute,
    overflow.hidden,
    top(10 px),
    width(315 px),
    display.flex,
    flexDirection.column,
    alignItems.center,
    borderRadius(6 px),
    backgroundColor(white),
    boxShadow := "rgba(0, 0, 0, 0.1) 0px 1px"
  )

  val colorOptions = style(
    display.flex,
    flexDirection.row,
    flexWrap.wrap,
    width(100 %%),
    padding(3 px),
    overflowY.scroll,
    maxHeight(108 px)
  )

  val colorOption = style(
    height(16 px),
    width(16 px),
    cursor.pointer,
    margin(2 px),
    borderRadius(4 px)
  )

  val colorDropdownTriangle = style(
    position.absolute,
    width(0 px),
    height(0 px),
    borderStyle(solid),
    borderWidth(0 px, 10 px, 10 px),
    borderColor(transparent, transparent, transparent)
  )

  val colorDropdownHeader = style(
    height(18 px),
    width(100 %%),
    display.flex,
    alignItems.center,
    justifyContent.center,
    fontSize(0.86 em)
  )

  val fairUse = style(
    margin(6 px),
    color(c"#484646"),
    textAlign.center,
    maxWidth(800 px)
  )

  val fairUseContainer = style(
    position.absolute,
    bottom(0 px),
    display.flex,
    flexDirection.column,
    alignItems.center,
    width(100 %%),
    zIndex(10)
  )
}
