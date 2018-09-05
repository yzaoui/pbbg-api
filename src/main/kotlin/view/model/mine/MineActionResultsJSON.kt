package com.bitwiserain.pbbg.view.model.mine

import com.bitwiserain.pbbg.domain.model.mine.MineActionResult
import com.google.gson.annotations.SerializedName

/**
 * Represents the results of a mining action.
 *
 * @property results The list containing an entry for every kind of obtained item with its corresponding quantity.
 */
data class MineActionResultsJSON(
    @SerializedName("results") val results: List<MineActionResult> // TODO: Using domain model directly
)
