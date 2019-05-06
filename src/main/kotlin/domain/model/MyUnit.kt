package com.bitwiserain.pbbg.domain.model

import com.bitwiserain.pbbg.domain.model.MyUnitEnum.*
import kotlin.math.max

sealed class MyUnit {
    abstract val id: Long
    abstract val enum: MyUnitEnum
    abstract val hp: Int
    abstract val maxHP: Int
    abstract val atk: Int
    abstract val exp: Long
    val alive: Boolean
        get() = hp > 0
    val dead: Boolean
        get() = !alive

    data class IceCreamWizard(
        override val id: Long, override val hp: Int, override val maxHP: Int, override val atk: Int,
        override val exp: Long
    ) : MyUnit() {
        override val enum get() = ICE_CREAM_WIZARD
    }

    data class Twolip(
        override val id: Long, override val hp: Int, override val maxHP: Int, override val atk: Int,
        override val exp: Long
    ) : MyUnit() {
        override val enum get() = TWOLIP
    }

    data class Carpshooter(
        override val id: Long, override val hp: Int, override val maxHP: Int, override val atk: Int,
        override val exp: Long
    ) : MyUnit() {
        override val enum get() = CARPSHOOTER
    }

    fun receiveDamage(damage: Int): MyUnit {
        val newHp = max(hp - damage, 0)

        return when (this) {
            is IceCreamWizard -> copy(hp = newHp)
            is Twolip -> copy(hp = newHp)
            is Carpshooter -> copy(hp = newHp)
        }
    }

    fun gainExperience(gainedExp: Long): MyUnit {
        val newExp = exp + gainedExp

        return when (this) {
            is IceCreamWizard -> copy(exp = newExp)
            is Twolip -> copy(exp = newExp)
            is Carpshooter -> copy(exp = newExp)
        }
    }

    fun maxHeal(): MyUnit {
        return when (this) {
            is IceCreamWizard -> copy(hp = maxHP)
            is Twolip -> copy(hp = maxHP)
            is Carpshooter -> copy(hp = maxHP)
        }
    }
}
