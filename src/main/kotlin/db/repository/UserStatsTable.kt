package com.bitwiserain.pbbg.db.repository

import org.jetbrains.exposed.sql.Table

object UserStatsTable : Table() {
    val userId = reference("user_id", UserTable)
    val miningExp = long("mining_exp").default(0)
}
