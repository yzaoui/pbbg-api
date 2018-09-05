package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.mine.MineEntity
import org.jetbrains.exposed.dao.IntIdTable

object MineCellTable : IntIdTable() {
    val mineId = reference("mine_id", MineSessionTable)
    val x = integer("x")
    val y = integer("y")
    val mineEntity = enumeration("mine_entity", MineEntity::class.java)
}
