package com.bitwiserain.pbbg.domain.model.dex

import com.bitwiserain.pbbg.domain.model.BaseItem

sealed class DexItem {
    data class DiscoveredDexItem(val baseItem: BaseItem) : DexItem()
    data class UndiscoveredDexItem(val id: Int) : DexItem()
}
