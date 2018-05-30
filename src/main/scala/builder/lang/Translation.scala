package builder.lang

trait Translation {
  def mapping: Map[String, String]

  final def getByKey(key: String): String = {
    mapping.getOrElse(key, key)
  }

  final def characterClass(key: String): String =
    getByKey("class." + key)

  final def characteristic(key: String): String =
    getByKey("charac." + key)

  final def characteristicDescription(key: String): String =
    getByKey("charac_desc." + key)

  final def skill(key: String): String =
    getByKey("skill." + key)

  final def itemRarity(key: String): String =
    getByKey("rarity." + key)

  final def itemType(key: String): String =
    getByKey("item." + key)

  final def itemRune(key: String): String =
    getByKey("rune." + key)

  final def sex(key: String): String =
    getByKey("sex." + key)

  final def element(key: String): String =
    getByKey("element." + key)

  final def ui(key: String): String =
    getByKey("ui." + key)
}
