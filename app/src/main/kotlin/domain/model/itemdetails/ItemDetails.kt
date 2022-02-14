package com.bitwiserain.pbbg.app.domain.model.itemdetails

import com.bitwiserain.pbbg.app.domain.model.MaterializedItem

class ItemDetails(
    val item: MaterializedItem,
    val history: List<ItemHistory>,
    val linkedUserInfo: Map<Int, String>
)
