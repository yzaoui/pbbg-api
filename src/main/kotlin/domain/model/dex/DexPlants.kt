package com.bitwiserain.pbbg.domain.model.dex

import com.bitwiserain.pbbg.domain.model.farm.BasePlant

/**
 * The user's plants dex, which lists all plant types that the user has ever harvested.
 *
 * @property discoveredPlants The plants that the user has ever harvested.
 * @property lastPlantId The last possible plant ID.
 */
data class DexPlants(
    val discoveredPlants: Map<Int, BasePlant>,
    val lastPlantId: Int
)
