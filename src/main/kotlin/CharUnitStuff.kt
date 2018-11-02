package com.bitwiserain.pbbg

import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.domain.model.LevelProgress
import com.bitwiserain.pbbg.domain.model.MyUnit
import com.bitwiserain.pbbg.domain.model.MyUnit.*
import com.bitwiserain.pbbg.domain.model.MyUnitEnum.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.max

interface UnitUC {
    fun getSquad(userId: Int): Squad
}

class UnitUCImpl(private val db: Database) : UnitUC {
    override fun getSquad(userId: Int): Squad = transaction(db) {
        val allies = SquadTable.getAllies(userId)

        Squad(allies)
    }
}

class Squad(val units: List<MyUnit>)

fun addUnitToSquad(user: EntityID<Int>, unit: MyUnit) {
    val unitId = UnitTable.insertUnitAndGetId(unit)

    SquadTable.insert {
        it[SquadTable.user] = user
        it[SquadTable.unit] = unitId
    }
}

fun ResultRow.toCharUnit(): MyUnit {
    val id = this[UnitTable.id].value
    val unitEnum = this[UnitTable.unit]
    val hp = this[UnitTable.hp]
    val maxHP = this[UnitTable.maxHP]
    val atk = this[UnitTable.atk]
    val exp = this[UnitTable.exp]

    return when (unitEnum) {
        ICE_CREAM_WIZARD -> IceCreamWizard(id, hp, maxHP, atk, exp)
        TWOLIP -> Twolip(id, hp, maxHP, atk, exp)
        CARPSHOOTER -> Carpshooter(id, hp, maxHP, atk, exp)
    }
}

fun SquadTable.getAllies(userId: Int): List<MyUnit> {
    return innerJoin(UnitTable)
        .slice(UnitTable.columns)
        .select { SquadTable.user.eq(userId) }
        .map { it.toCharUnit() }
}

fun SquadTable.getAlly(userId: Int, allyId: Long): MyUnit? {
    return innerJoin(UnitTable)
        .slice(UnitTable.columns)
        .select { SquadTable.user.eq(userId) and UnitTable.id.eq(allyId) }
        .singleOrNull()
        ?.toCharUnit()
}

fun UnitTable.insertUnitAndGetId(unit: MyUnit): EntityID<Long> {
    return insertAndGetId {
        it[UnitTable.unit] = unit.enum
        it[UnitTable.hp] = unit.hp
        it[UnitTable.maxHP] = unit.maxHP
        it[UnitTable.atk] = unit.atk
        it[UnitTable.exp] = unit.exp
    }
}

fun UnitTable.updateUnit(unitId: Long, unit: MyUnit) {
    update({ UnitTable.id.eq(unitId) }) {
        it[UnitTable.hp] = unit.hp
        it[UnitTable.maxHP] = unit.maxHP
        it[UnitTable.atk] = unit.atk
        it[UnitTable.exp] = unit.exp
    }
}

fun MyUnit.receiveDamage(damage: Int): MyUnit {
    val newHp = max(hp - damage, 0)

    return when (this) {
        is IceCreamWizard -> copy(hp = newHp)
        is Twolip -> copy(hp = newHp)
        is Carpshooter -> copy(hp = newHp)
    }
}

fun MyUnit.gainExperience(gainedExp: Long): MyUnit {
    val newExp = exp + gainedExp

    return when (this) {
        is IceCreamWizard -> copy(exp = newExp)
        is Twolip -> copy(exp = newExp)
        is Carpshooter -> copy(exp = newExp)
    }
}

abstract class ExperienceManager {
    /**
     * A list where every value represents the minimum absolute amount of experience to reach the next level.
     */
    protected abstract val levels: List<Long>

    fun getLevelProgress(absoluteExp: Long): LevelProgress {
        // Previous threshold
        var prevLevelAbsoluteExp = 0L

        for ((i, absoluteExpToLevelUp) in levels.withIndex()) {
            // Since levels start at 1 not 0
            val level = i + 1

            if (absoluteExp < absoluteExpToLevelUp) {
                return LevelProgress(
                    level = level,
                    absoluteExp = absoluteExp,
                    absoluteExpCurrentLevel = prevLevelAbsoluteExp,
                    absoluteExpNextLevel = absoluteExpToLevelUp
                )
            }

            prevLevelAbsoluteExp = absoluteExpToLevelUp
        }

        return LevelProgress(
            level = levels.size + 1,
            absoluteExp = levels.last(),
            absoluteExpCurrentLevel = levels.last(),
            absoluteExpNextLevel = levels.last()
        )
    }
}

object UnitExperienceManager : ExperienceManager() {
    override val levels = listOf(10L, 24L)
}
