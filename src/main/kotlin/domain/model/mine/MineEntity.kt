package com.bitwiserain.pbbg.domain.model.mine

/**
 * Entities that can be found on the mine's surface.
 */
enum class MineEntity(val exp: Int, val spriteName: String) {
    ROCK(4, "rock"),
    COAL(9, "coal"),
    COPPER(8, "copper")
}
