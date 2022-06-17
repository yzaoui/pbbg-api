package com.bitwiserain.pbbg.app.view.model.mine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MineTypeListJSON(
    @SerialName("types") val types: List<MineTypeJSON>,
    @SerialName("nextUnlockLevel") val nextUnlockLevel: Int?
)
