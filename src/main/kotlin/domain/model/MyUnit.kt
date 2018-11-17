package com.bitwiserain.pbbg.domain.model

import com.bitwiserain.pbbg.domain.model.MyUnitEnum.*
import kotlin.math.max

sealed class MyUnit {
    abstract val id: Long
    abstract val name: String
    abstract val enum: MyUnitEnum
    abstract val hp: Int
    abstract val maxHP: Int
    abstract val atk: Int
    abstract val exp: Long
    val dead: Boolean
        get() = hp == 0

    data class IceCreamWizard(
        override val id: Long, override val hp: Int, override val maxHP: Int, override val atk: Int,
        override val exp: Long
    ) : MyUnit() {
        override val name: String get() = "Ice-Cream Wizard"
        override val enum get() = ICE_CREAM_WIZARD
    }

    data class Twolip(
        override val id: Long, override val hp: Int, override val maxHP: Int, override val atk: Int,
        override val exp: Long
    ) : MyUnit() {
        override val name: String get() = "Twolip"
        override val enum get() = TWOLIP
    }

    data class Carpshooter(
        override val id: Long, override val hp: Int, override val maxHP: Int, override val atk: Int,
        override val exp: Long
    ) : MyUnit() {
        override val name: String get() = "Carpshooter"
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
}

enum class MyUnitEnum {
    ICE_CREAM_WIZARD,
    TWOLIP,
    CARPSHOOTER
}