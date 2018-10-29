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

object BattleEnemyTable : Table() {
    val battle = reference("battle_session_id", BattleSessionTable)
    val unit = reference("unit_id", UnitTable)
}

interface BattleUC {
    fun getCurrentBattle(userId: Int): Battle?
    fun generateBattle(userId: Int): Battle
    fun attack(userId: Int, allyId: Long, enemyId: Long)
}

class BattleUCImpl(private val db: Database) : BattleUC {
    override fun getCurrentBattle(userId: Int): Battle? = transaction(db) {
        val battleSession = BattleSessionTable.getBattleSessionId(userId)

        if (battleSession == null) return@transaction null

        val allies = SquadTable.getAllies(userId)

        val enemies = BattleEnemyTable.getEnemies(battleSession)

        Battle(allies = allies, enemies = enemies)
    }

    override fun generateBattle(userId: Int): Battle = transaction(db) {
        // TODO: Forbid action if a battle is already in progress

        val enemies = listOf(
            IceCreamWizard(0, 10, 10, 2),
            Twolip(0, 16, 16, 3)
        )

        val battleSession = BattleSessionTable.insertAndGetId {
            it[BattleSessionTable.userId] = EntityID(userId, UserTable)
        }

        BattleEnemyTable.insertEnemies(battleSession, enemies)

        val allies = SquadTable.getAllies(userId)

        Battle(allies = allies, enemies = enemies)
    }

    override fun attack(userId: Int, allyId: Long, enemyId: Long) = transaction(db) {
        val battleSession = BattleSessionTable.getBattleSessionId(userId) ?: throw Exception()

        val ally = SquadTable.getAlly(userId, allyId) ?: throw Exception()
        val enemy = BattleEnemyTable.getEnemy(battleSession, enemyId) ?: throw Exception()
        val newEnemy = enemy.receiveDamage(ally.atk)

        UnitTable.updateUnit(enemyId, newEnemy)
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

fun BattleEnemyTable.insertEnemies(battleSession: EntityID<Long>, enemies: List<CharUnit>) {
    // TODO: There's gotta be a way to do this in batch :/
    for (enemy in enemies) {
        // Create enemy unit in unit table
        val enemyId = UnitTable.insertUnitAndGetId(enemy)

        // Connect newly created enemy to this battle session
        insert {
            it[BattleEnemyTable.battle] = battleSession
            it[BattleEnemyTable.unit] = enemyId
        }
    }
}

fun BattleEnemyTable.getEnemies(battleSession: EntityID<Long>): List<CharUnit> {
    return innerJoin(UnitTable)
        .slice(UnitTable.columns)
        .select { BattleEnemyTable.battle.eq(battleSession) }
        .map { it.toCharUnit() }
}

fun BattleEnemyTable.getEnemy(battleSession: EntityID<Long>, enemyId: Long): CharUnit? {
    return innerJoin(UnitTable)
        .slice(UnitTable.columns)
        .select { BattleEnemyTable.battle.eq(battleSession) and UnitTable.id.eq(enemyId) }
        .singleOrNull()
        ?.toCharUnit()
}

fun BattleSessionTable.getBattleSessionId(userId: Int): EntityID<Long>? {
    return select { BattleSessionTable.userId.eq(userId) }
        .singleOrNull()
        ?.get(BattleSessionTable.id)
}
