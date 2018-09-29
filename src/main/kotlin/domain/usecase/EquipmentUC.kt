package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Pickaxe

interface EquipmentUC {
    fun getEquippedPickaxe(userId: Int): Pickaxe?
    fun generatePickaxe(userId: Int): Pickaxe?
}
