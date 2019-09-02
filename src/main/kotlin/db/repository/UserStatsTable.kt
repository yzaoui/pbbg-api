package com.bitwiserain.pbbg.db.repository

import org.jetbrains.exposed.sql.Table

object UserStatsTable : Table() {
    val userId = reference("user_id", UserTable)
    val gold = long("gold").default(0)
    val miningExp = long("mining_exp").default(0)
}
