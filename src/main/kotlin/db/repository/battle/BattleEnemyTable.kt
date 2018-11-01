package com.bitwiserain.pbbg.db.repository.battle

import com.bitwiserain.pbbg.db.repository.UnitTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object BattleEnemyTable : Table() {
    val battle = reference("battle_session_id", BattleSessionTable, ReferenceOption.CASCADE)
    val unit = reference("unit_id", UnitTable, ReferenceOption.CASCADE)
}
