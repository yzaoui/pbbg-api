package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class MyUnitEnumJSON(
    @SerializedName("id") val id: Int,
    @SerializedName("friendlyName") val friendlyName: String,
    @SerializedName("description") val description: String,
    @SerializedName("iconURL") val iconURL: String,
    @SerializedName("fullURL") val fullURL: String,
    @SerializedName("baseHP") val baseHP: Int,
    @SerializedName("baseAtk") val baseAtk: Int,
    @SerializedName("baseDef") val baseDef: Int,
    @SerializedName("baseInt") val baseInt: Int,
    @SerializedName("baseRes") val baseRes: Int
)
