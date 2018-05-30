package builder.state

import monocle.Optional
import monocle.function.Index
import monocle.macros.Lenses
import org.bitbucket.wakfuthesaurus.shared.data.Spell

@Lenses
final case class SpellsState(
  actives: List[Option[Spell]] = List.fill(12)(None),
  passives: List[Option[Spell]] = List.fill(6)(None)
)

object SpellsState {
  type SpellLens = Optional[SpellsState, Option[Spell]]

  val firstActiveRow: Seq[SpellLens] = 0 until 6 map activeLens
  val secondActiveRow: Seq[SpellLens] = 6 until 12 map activeLens
  val passiveRow: Seq[SpellLens] = 0 until 6 map passiveLens

  def activeLens(index: Int): Optional[SpellsState, Option[Spell]] =
    SpellsState.actives ^|-? Index.listIndex[Option[Spell]].index(index)

  def passiveLens(index: Int): Optional[SpellsState, Option[Spell]] =
    SpellsState.passives ^|-? Index.listIndex[Option[Spell]].index(index)
}