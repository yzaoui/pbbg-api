package com.bitwiserain.pbbg.app.domain.model.dex

import com.bitwiserain.pbbg.app.domain.model.BaseItem

sealed class DexItem {
    data class DiscoveredDexItem(val baseItem: BaseItem) : DexItem()
    data class UndiscoveredDexItem(val id: Int) : DexItem()
}
