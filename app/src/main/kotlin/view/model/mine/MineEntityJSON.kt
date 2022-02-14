package com.bitwiserain.pbbg.app.view.model.mine

import com.google.gson.annotations.SerializedName

/**
 * Represents an entity within a mine cell.
 *
 * @property name The name of this entity.
 * @property imageURL The URL to get this entity's image representation.
 */
data class MineEntityJSON(
    @SerializedName("name") val name: String,
    @SerializedName("imageURL") val imageURL: String
)
