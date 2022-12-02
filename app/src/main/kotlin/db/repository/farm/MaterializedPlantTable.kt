package com.bitwiserain.pbbg.app.db.repository.farm

import com.bitwiserain.pbbg.app.db.repository.farm.MaterializedPlantTable.PlantForm
import com.bitwiserain.pbbg.app.domain.model.farm.MaterializedPlant
import com.bitwiserain.pbbg.app.domain.model.farm.PlantEnum
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import java.time.Instant

interface MaterializedPlantTable {

    fun insertPlantAndGetId(plant: PlantForm): Long

    fun setNewPlantCycleAndHarvest(plantId: Long, newCycleStart: Instant, harvests: Int)

    fun deletePlant(plantId: Long)

    data class PlantForm(
        val enum: PlantEnum,
        val cycleStart: Instant,
        val isMaturable: Boolean
    )
}

class MaterializedPlantTableImpl : MaterializedPlantTable {

    object Exposed : LongIdTable(name = "MaterializedPlant") {

        val plantEnum = enumeration("plant_enum_ordinal", PlantEnum::class)
        val cycleStart = long("cycle_start")
        val harvests = integer("harvests").nullable()
    }

    override fun insertPlantAndGetId(plant: PlantForm): Long = Exposed.insertAndGetId {
        it[Exposed.plantEnum] = plant.enum
        it[Exposed.cycleStart] = plant.cycleStart.epochSecond
        if (plant.isMaturable) it[Exposed.harvests] = 0
    }.value

    override fun setNewPlantCycleAndHarvest(plantId: Long, newCycleStart: Instant, harvests: Int) {
        Exposed.update({ Exposed.id.eq(plantId) }) {
            it[Exposed.cycleStart] = newCycleStart.epochSecond
            it[Exposed.harvests] = harvests
        }
    }

    override fun deletePlant(plantId: Long) {
        Exposed.deleteWhere { Exposed.id.eq(plantId) }
    }
}

fun ResultRow.toMaterializedPlant(): MaterializedPlant {
    val plantEnum = this[MaterializedPlantTableImpl.Exposed.plantEnum]
    val cycleStart = Instant.ofEpochSecond(this[MaterializedPlantTableImpl.Exposed.cycleStart])
    val harvests = this[MaterializedPlantTableImpl.Exposed.harvests]

    return when (plantEnum) {
        PlantEnum.APPLE_TREE -> MaterializedPlant.AppleTree(cycleStart, harvests!!)
        PlantEnum.TOMATO_PLANT -> MaterializedPlant.TomatoPlant(cycleStart)
    }
}
