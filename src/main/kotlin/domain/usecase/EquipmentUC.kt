package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.Pickaxe

interface EquipmentUC {
    fun getEquippedPickaxe(userId: Int): Pickaxe?
    fun generatePickaxe(userId: Int): Item.Pickaxe? // TODO: Temporary use case until proper way to obtain pickaxe is implemented
}
