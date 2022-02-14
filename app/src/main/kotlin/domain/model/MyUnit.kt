package com.bitwiserain.pbbg.app.domain.model

import com.bitwiserain.pbbg.app.domain.BattleManager
import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum.*

// TODO: Maybe get rid of this sealed class stuff and just have a default unit data class with an enum property?
sealed class MyUnit {
    abstract val id: Long
    abstract val enum: MyUnitEnum
    abstract val hp: Int
    abstract val maxHP: Int
    abstract val atk: Int
    abstract val def: Int
    abstract val int: Int
    abstract val res: Int
    abstract val exp: Long
    val alive: Boolean
        get() = hp > 0
    val dead: Boolean
        get() = !alive

    data class IceCreamWizard(
        override val id: Long, override val hp: Int, override val maxHP: Int, override val atk: Int,
        override val def: Int, override val int: Int, override val res: Int, override val exp: Long
    ) : MyUnit() {
        override val enum get() = ICE_CREAM_WIZARD
    }

    data class Twolip(
        override val id: Long, override val hp: Int, override val maxHP: Int, override val atk: Int,
        override val def: Int, override val int: Int, override val res: Int, override val exp: Long
    ) : MyUnit() {
        override val enum get() = TWOLIP
    }

    data class Carpshooter(
        override val id: Long, override val hp: Int, override val maxHP: Int, override val atk: Int,
        override val def: Int, override val int: Int, override val res: Int, override val exp: Long
    ) : MyUnit() {
        override val enum get() = CARPSHOOTER
    }

    data class Flamango(
        override val id: Long, override val hp: Int, override val maxHP: Int, override val atk: Int,
        override val def: Int, override val int: Int, override val res: Int, override val exp: Long
    ) : MyUnit() {
        override val enum get() = FLAMANGO
    }

    fun receiveDamage(attackerAtk: Int): DamageResult {
        val damage = BattleManager.calculateDamage(attackerAtk, this.def)
        val newHp = (hp - damage).coerceIn(0..this.maxHP)

        val updatedUnit = when (this) {
            is IceCreamWizard -> copy(hp = newHp)
            is Twolip -> copy(hp = newHp)
            is Carpshooter -> copy(hp = newHp)
            is Flamango -> copy(hp = newHp)
        }

        return DamageResult(updatedUnit, damage)
    }

    fun gainExperience(gainedExp: Long): MyUnit {
        val newExp = exp + gainedExp

        return when (this) {
            is IceCreamWizard -> copy(exp = newExp)
            is Twolip -> copy(exp = newExp)
            is Carpshooter -> copy(exp = newExp)
            is Flamango -> copy(exp = newExp)
        }
    }

    fun maxHeal(): MyUnit {
        return when (this) {
            is IceCreamWizard -> copy(hp = maxHP)
            is Twolip -> copy(hp = maxHP)
            is Carpshooter -> copy(hp = maxHP)
            is Flamango -> copy(hp = maxHP)
        }
    }
}

data class DamageResult(val updatedUnit: MyUnit, val damage: Int)
