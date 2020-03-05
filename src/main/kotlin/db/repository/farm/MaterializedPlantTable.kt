package com.bitwiserain.pbbg.db.repository.farm

import com.bitwiserain.pbbg.domain.model.farm.MaterializedPlant
import com.bitwiserain.pbbg.domain.model.farm.PlantEnum
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import java.time.Instant

object MaterializedPlantTable : LongIdTable() {
    val plantEnum = enumeration("plant_enum_ordinal", PlantEnum::class)
    val cycleStart = long("cycle_start")
    val isFirstHarvest = bool("is_first_harvest").nullable()

    fun insertPlantAndGetId(plant: PlantForm): Long = insertAndGetId {
        it[MaterializedPlantTable.plantEnum] = plant.enum
        it[MaterializedPlantTable.cycleStart] = plant.cycleStart.epochSecond
        if (plant.isMaturable) it[MaterializedPlantTable.isFirstHarvest] = true
    }.value

    fun setNewPlantCycleAndHarvest(plantId: Long, newCycleStart: Instant) {
        update({ MaterializedPlantTable.id.eq(plantId) }) {
            it[MaterializedPlantTable.cycleStart] = newCycleStart.epochSecond
            it[MaterializedPlantTable.isFirstHarvest] = false
        }
    }

    fun deletePlant(plantId: Long) {
        deleteWhere { MaterializedPlantTable.id.eq(plantId) }
    }

    fun ResultRow.toMaterializedPlant(): MaterializedPlant {
        val plantEnum = this[MaterializedPlantTable.plantEnum]
        val cycleStart = Instant.ofEpochSecond(this[MaterializedPlantTable.cycleStart])
        val isFirstHarvest = this[MaterializedPlantTable.isFirstHarvest]

        return when (plantEnum) {
            PlantEnum.APPLE_TREE -> MaterializedPlant.AppleTree(cycleStart, isFirstHarvest!!)
            PlantEnum.TOMATO_PLANT -> MaterializedPlant.TomatoPlant(cycleStart)
        }
    }

    data class PlantForm(
        val enum: PlantEnum,
        val cycleStart: Instant,
        val isMaturable: Boolean
    )
}
