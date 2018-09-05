package com.bitwiserain.pbbg.view.model.mine

import com.google.gson.annotations.SerializedName

/**
 * Represents an entity within a mine cell.
 *
 * @property imageURL The URL to get this entity's image representation.
 */
data class MineEntityJSON(
    @SerializedName("imageURL") val imageURL: String
)
