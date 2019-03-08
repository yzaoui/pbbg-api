package com.bitwiserain.pbbg.db.repository.mine

import com.bitwiserain.pbbg.domain.model.mine.MineEntity
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object MineCellTable : IntIdTable() {
    val mineId = reference("mine_id", MineSessionTable, ReferenceOption.CASCADE)
    val x = integer("x")
    val y = integer("y")
    val mineEntity = enumeration("mine_entity", MineEntity::class)
}
