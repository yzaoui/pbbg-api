package com.bitwiserain.pbbg.view.model.itemdetails

import com.bitwiserain.pbbg.view.model.MaterializedItemJSON

class ItemDetailsJSON(
    val item: MaterializedItemJSON,
    val history: List<ItemHistoryJSON>
)
