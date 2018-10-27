package com.bitwiserain.pbbg

import com.bitwiserain.pbbg.CharUnit.*
import com.bitwiserain.pbbg.CharUnitEnum.*
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.route.api.CharUnitJSON
import com.google.gson.annotations.SerializedName
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BattleSessionTable : LongIdTable() {
    val userId = reference("user_id", UserTable)
}

object BattleEnemyTable : LongIdTable() {
    val battle = reference("battle_session_id", BattleSessionTable)
    val unit = enumeration("unit", CharUnitEnum::class)
    val atk = integer("atk")
    val def = integer("def")
}

interface BattleUC {
    fun getCurrentBattle(userId: Int): Battle?
    fun generateBattle(userId: Int): Battle
}

class BattleUCImpl(private val db: Database) : BattleUC {
    override fun getCurrentBattle(userId: Int): Battle? = transaction(db) {
        val battleSession = BattleSessionTable.select { BattleSessionTable.userId.eq(userId) }
            .singleOrNull()
            ?.get(BattleSessionTable.id)

        if (battleSession == null) return@transaction null

        val allies = SquadTable.getAllies(userId)

        val enemies = BattleEnemyTable.select { BattleEnemyTable.battle.eq(battleSession) }
            .map { it.toCharUnit() }

        Battle(allies = allies, enemies = enemies)
    }

    override fun generateBattle(userId: Int): Battle = transaction(db) {
        // TODO: Forbid action if a battle is already in progress

        val enemies = listOf(
            IceCreamWizard(2, 5),
            Twolip(3, 1)
        )

        val battleSession = BattleSessionTable.insertAndGetId {
            it[BattleSessionTable.userId] = EntityID(userId, UserTable)
        }

        BattleEnemyTable.insertEnemies(battleSession, enemies)

        val allies = SquadTable.getAllies(userId)

        Battle(allies = allies, enemies = enemies)
    }
}

class Battle(
    val allies: List<CharUnit>,
    val enemies: List<CharUnit>
)

class BattleJSON(
    @SerializedName("allies") val allies: List<CharUnitJSON>,
    @SerializedName("enemies") val enemies: List<CharUnitJSON>
)

fun BattleEnemyTable.insertEnemies(battleSession: EntityID<Long>, enemies: List<CharUnit>) = batchInsert(enemies) {
    this[BattleEnemyTable.battle] = battleSession
    this[BattleEnemyTable.unit] = it.enum
    this[BattleEnemyTable.atk] = it.atk
    this[BattleEnemyTable.def] = it.def
}

private fun ResultRow.toCharUnit(): CharUnit {
    val unitEnum = this[BattleEnemyTable.unit]
    val atk = this[BattleEnemyTable.atk]
    val def = this[BattleEnemyTable.def]

    return when (unitEnum) {
        ICE_CREAM_WIZARD -> IceCreamWizard(atk, def)
        TWOLIP -> Twolip(atk, def)
        CARPSHOOTER -> Carpshooter(atk, def)
    }
}
