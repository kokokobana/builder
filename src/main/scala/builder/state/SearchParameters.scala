package builder.state

import org.bitbucket.wakfuthesaurus.shared.data.{ItemEffectFilter, ItemFilter, ItemRarity}

final case class SearchParameters(
  name: Option[String] = None,
  typeFilters: Seq[ItemFilter] = Seq(ItemFilter.Any),
  effectFilters: Seq[ItemEffectFilter] = Seq(ItemEffectFilter.NoFilter),
  rarities: Seq[ItemRarity] = Seq.empty,
  minLevel: Int = 0,
  maxLevel: Int = 200,
  onlyBestCraft: Boolean = true
)
