package com.bitwiserain.pbbg.db.repository.farm

import com.bitwiserain.pbbg.domain.model.farm.MaterializedPlant
import com.bitwiserain.pbbg.domain.model.farm.PlantEnum
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import java.time.Instant

object MaterializedPlantTable : LongIdTable() {
    val plantEnum = enumeration("plant_enum_ordinal", PlantEnum::class)
    val cycleStart = long("cycle_start")
    val harvests = integer("harvests").nullable()

    fun insertPlantAndGetId(plant: PlantForm): Long = insertAndGetId {
        it[MaterializedPlantTable.plantEnum] = plant.enum
        it[MaterializedPlantTable.cycleStart] = plant.cycleStart.epochSecond
        if (plant.isMaturable) it[MaterializedPlantTable.harvests] = 0
    }.value

    fun setNewPlantCycleAndHarvest(plantId: Long, newCycleStart: Instant, harvests: Int) {
        update({ MaterializedPlantTable.id.eq(plantId) }) {
            it[MaterializedPlantTable.cycleStart] = newCycleStart.epochSecond
            it[MaterializedPlantTable.harvests] = harvests
        }
    }

    fun deletePlant(plantId: Long) {
        deleteWhere { MaterializedPlantTable.id.eq(plantId) }
    }

    fun ResultRow.toMaterializedPlant(): MaterializedPlant {
        val plantEnum = this[MaterializedPlantTable.plantEnum]
        val cycleStart = Instant.ofEpochSecond(this[MaterializedPlantTable.cycleStart])
        val harvests = this[MaterializedPlantTable.harvests]

        return when (plantEnum) {
            PlantEnum.APPLE_TREE -> MaterializedPlant.AppleTree(cycleStart, harvests!!)
            PlantEnum.TOMATO_PLANT -> MaterializedPlant.TomatoPlant(cycleStart)
        }
    }

    data class PlantForm(
        val enum: PlantEnum,
        val cycleStart: Instant,
        val isMaturable: Boolean
    )
}
