package com.bitwiserain.pbbg.view.model.mine

import com.bitwiserain.pbbg.view.model.PointJSON
import com.google.gson.annotations.SerializedName

/**
 * Represents a pickaxe in the context of mining.
 *
 * @property pickaxeKind The name for this kind of pickaxe, such as "Bronze" or "Pointy", to build a string such as "[pickaxeKind] Pickaxe".
 * @property cells A list of the cells this pickaxe can reach, with each cell in the format of [x, y], relative to its center at [0, 0].
 */
data class PickaxeJSON(
    @SerializedName("pickaxeKind") val pickaxeKind: String,
    @SerializedName("cells") val cells: Set<PointJSON>
)
