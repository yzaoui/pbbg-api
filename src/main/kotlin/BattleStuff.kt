package com.bitwiserain.pbbg

import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.route.api.CharUnitJSON
import com.google.gson.annotations.SerializedName
import org.jetbrains.exposed.dao.LongIdTable

object BattleTable : LongIdTable() {
    val userId = reference("user_id", UserTable)
}

object BattleEnemyTable : LongIdTable() {
    val battle = reference("battle_id", BattleTable)
    val unit = enumeration("unit", CharUnitEnum::class)
    val atk = integer("atk")
    val def = integer("def")
}

interface BattleUC {
    fun getCurrentBattle(userId: Int): Battle?
    fun generateBattle(userId: Int): Battle
}

class Battle(
    val allies: List<CharUnit>,
    val enemies: List<CharUnit>
)

class BattleJSON(
    @SerializedName("allies") val allies: List<CharUnitJSON>,
    @SerializedName("enemies") val enemies: List<CharUnitJSON>
)
