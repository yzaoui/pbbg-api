package com.bitwiserain.pbbg.app.view.model.mine

import com.bitwiserain.pbbg.app.view.model.LevelProgressJSON
import com.bitwiserain.pbbg.app.view.model.LevelUpJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MineActionResultJSON(
    @SerialName("minedItemResults") val minedItemResults: List<MinedItemResultJSON>,
    @SerialName("levelUps") val levelUps: List<LevelUpJSON>,
    @SerialName("mine") val mine: MineJSON,
    @SerialName("miningLvl") val miningLvl: LevelProgressJSON
)
