package builder

import builder.lang.{Localization, Translation}
import builder.util.AssetFactory

final case class Context(
  translation: Translation,
  api: Service,
  s3URL: String
) {
  val localization: Localization = new Localization(translation)
  val assets: AssetFactory = new AssetFactory(s3URL)
}