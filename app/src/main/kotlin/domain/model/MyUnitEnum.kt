package com.bitwiserain.pbbg.app.domain.model

enum class MyUnitEnum(
    val friendlyName: String,
    val spriteName: String,
    val description: String,
    val baseHP: Int,
    val baseAtk: Int,
    val baseDef: Int,
    val baseInt: Int,
    val baseRes: Int
) {
    ICE_CREAM_WIZARD(
        "Ice-Cream Wizard",
        "ice-cream-wizard",
        "Trained in the mystic arts of dairy goodness, this wizard can swirl up quite the storm.",
        baseHP = 19,
        baseAtk = 8,
        baseDef = 9,
        baseInt = 15,
        baseRes = 13

    ),
    TWOLIP(
        "Twolip",
        "twolip",
        "It's a manifestation of the essence of love. Don't be so quick to fall for its gentle appearance—" +
                "the edges of its leaves can be as painful as heartbreak.",
        baseHP = 18,
        baseAtk = 15,
        baseDef = 7,
        baseInt = 9,
        baseRes = 8
    ),
    CARPSHOOTER(
        "Carpshooter",
        "carpshooter",
        "Carpshooter uses the skills it has honed through determination and its sense of duty. This allows it to " +
                "overcome its clear lack of a sense of smell.",
        baseHP = 20,
        baseAtk = 12,
        baseDef = 12,
        baseInt = 10,
        baseRes = 9
    ),
    FLAMANGO(
        "Flamango",
        "flamango",
        "This tropical avian isn't shy to pecking away any unwanted attention its colorful exterior and wonderful " +
                "scent attract. Give it a sniff at your own risk.",
        baseHP = 22,
        baseAtk = 13,
        baseDef = 14,
        baseInt = 8,
        baseRes = 6
    )
}
