package com.bitwiserain.pbbg.app.view.model.mine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an entity within a mine cell.
 *
 * @property name The name of this entity.
 * @property imageURL The URL to get this entity's image representation.
 */
@Serializable
data class MineEntityJSON(
    @SerialName("name") val name: String,
    @SerialName("imageURL") val imageURL: String
)
