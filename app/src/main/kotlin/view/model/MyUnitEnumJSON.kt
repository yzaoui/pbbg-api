package com.bitwiserain.pbbg.app.view.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyUnitEnumJSON(
    @SerialName("id") val id: Int,
    @SerialName("friendlyName") val friendlyName: String,
    @SerialName("description") val description: String,
    @SerialName("iconURL") val iconURL: String,
    @SerialName("fullURL") val fullURL: String,
    @SerialName("baseHP") val baseHP: Int,
    @SerialName("baseAtk") val baseAtk: Int,
    @SerialName("baseDef") val baseDef: Int,
    @SerialName("baseInt") val baseInt: Int,
    @SerialName("baseRes") val baseRes: Int
)
