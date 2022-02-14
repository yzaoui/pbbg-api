package com.bitwiserain.pbbg.app.view.model

import com.google.gson.annotations.SerializedName

data class MyUnitJSON(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("baseUnitId") val baseUnitId: Int,
    @SerializedName("hp") val hp: Int,
    @SerializedName("maxHP") val maxHP: Int,
    @SerializedName("atk") val atk: Int,
    @SerializedName("def") val def: Int,
    @SerializedName("levelProgress") val levelProgress: LevelProgressJSON,
    @SerializedName("idleAnimationURL") val idleAnimationURL: String,
    @SerializedName("iconURL") val iconURL: String
)
