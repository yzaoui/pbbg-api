package com.bitwiserain.pbbg.app.view.model.itemdetails

import com.bitwiserain.pbbg.app.view.model.MaterializedItemJSON

class ItemDetailsJSON(
    val item: MaterializedItemJSON,
    val history: List<ItemHistoryJSON>,
    val linkedUserInfo: Map<Int, String>
)
