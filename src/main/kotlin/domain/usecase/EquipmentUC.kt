package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Pickaxe

interface EquipmentUC {
    fun getPickaxe(userId: Int): Pickaxe?
    fun getAllPickaxes(): Array<Pickaxe>
    fun generatePickaxe(userId: Int): Pickaxe?
}
