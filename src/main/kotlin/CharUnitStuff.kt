package com.bitwiserain.pbbg

import com.bitwiserain.pbbg.db.repository.UserTable
import org.jetbrains.exposed.dao.LongIdTable

sealed class CharUnit {
    abstract val enum: CharUnitEnum
    abstract val atk: Int
    abstract val def: Int

    class IceCreamWizard(override val atk: Int, override val def: Int) : CharUnit() {
        override val enum get() = CharUnitEnum.ICE_CREAM_WIZARD
    }
}

enum class CharUnitEnum {
    ICE_CREAM_WIZARD
}

object CharUnitTable : LongIdTable() {
    val userId = reference("user_id", UserTable)
    val unit = enumeration("unit", CharUnitEnum::class)
}
