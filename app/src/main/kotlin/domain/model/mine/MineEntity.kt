package com.bitwiserain.pbbg.app.domain.model.mine

/**
 * Entities that can be found on the mine's surface.
 */
enum class MineEntity(val exp: Int, val friendlyName: String, val spriteName: String) {
    ROCK(4, "Rock","rock"),
    COAL(9, "Coal", "coal"),
    COPPER(8, "Copper", "copper")
}
