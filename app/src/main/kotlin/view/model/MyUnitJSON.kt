package com.bitwiserain.pbbg.app.view.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyUnitJSON(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("baseUnitId") val baseUnitId: Int,
    @SerialName("hp") val hp: Int,
    @SerialName("maxHP") val maxHP: Int,
    @SerialName("atk") val atk: Int,
    @SerialName("def") val def: Int,
    @SerialName("levelProgress") val levelProgress: LevelProgressJSON,
    @SerialName("idleAnimationURL") val idleAnimationURL: String,
    @SerialName("iconURL") val iconURL: String
)
