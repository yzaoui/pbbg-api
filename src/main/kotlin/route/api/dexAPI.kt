package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.API_ROOT
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.dex.DexItem
import com.bitwiserain.pbbg.domain.model.dex.DexItems
import com.bitwiserain.pbbg.domain.model.dex.DexPlants
import com.bitwiserain.pbbg.domain.model.dex.DexUnits
import com.bitwiserain.pbbg.domain.usecase.DexUC
import com.bitwiserain.pbbg.domain.usecase.InvalidItemException
import com.bitwiserain.pbbg.domain.usecase.InvalidUnitException
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.MyUnitEnumJSON
import com.bitwiserain.pbbg.view.model.dex.DexItemJSON
import com.bitwiserain.pbbg.view.model.dex.DexItemsJSON
import com.bitwiserain.pbbg.view.model.dex.DexPlantsJSON
import com.bitwiserain.pbbg.view.model.dex.DexUnitsJSON
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.dexAPI(dexUC: DexUC) = route("/dex") {
    route("/items/{id?}") {
        get {
            val itemEnumId = call.parameters["id"]?.toInt()

            if (itemEnumId == null) {
                // Calling for entire item dex
                val dex = dexUC.getDexItems(call.user.id)

                call.respondSuccess(dex.toJSON())
            } else {
                // Calling for specific item
                try {
                    val item = dexUC.getIndividualDexBaseItem(call.user.id, itemEnumId)

                    call.respondSuccess(item.toJSON())
                } catch (e: InvalidItemException) {
                    call.respondFail(HttpStatusCode.NotFound)
                }
            }
        }
    }

    route("/units/{id?}") {
        get {
            val unitEnumId = call.parameters["id"]?.toInt()

            if (unitEnumId == null) {
                // Calling for entire unit dex
                val dex = dexUC.getDexUnits(call.user.id)

                call.respondSuccess(dex.toJSON())
            } else {
                // Calling for specific unit
                try {
                    val unit = dexUC.getDexUnit(call.user.id, unitEnumId)

                    call.respondSuccess(unit.toJSON())
                } catch (e: InvalidUnitException) {
                    call.respondFail(HttpStatusCode.NotFound)
                }
            }
        }
    }

    route("/plants/{id?}") {
        get {
            val plantId = call.parameters["id"]?.toInt()

            if (plantId == null) {
                // Calling for entire plant dex
                val dex = dexUC.getDexPlants(call.user.id)

                call.respondSuccess(dex.toJSON())
            } else {
                // Calling for specific plant
                try {
                    val plant = dexUC.getDexPlant(call.user.id, plantId)

                    call.respondSuccess(plant.toJSON())
                } catch (e: Exception) {
                    call.respondFail(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

// TODO: Find appropriate place for this adapter
fun MyUnitEnum.toJSON() = MyUnitEnumJSON(
    id = ordinal + 1,
    friendlyName = friendlyName,
    description = description,
    fullURL = "$API_ROOT/img/unit/$spriteName.gif",
    iconURL = "$API_ROOT/img/unit-icon/$spriteName.png",
    baseHP = baseHP,
    baseAtk = baseAtk,
    baseDef = baseDef,
    baseInt = baseInt,
    baseRes = baseRes
)

private fun DexItems.toJSON(): DexItemsJSON = DexItemsJSON(
    discoveredItems = discoveredItems.associate { it.ordinal + 1 to it.baseItem.toJSON() }.toSortedMap(),
    lastItemId = lastItemId
)

private fun DexItem.toJSON(): DexItemJSON = when (this) {
    is DexItem.DiscoveredDexItem -> DexItemJSON.DiscoveredDexItemJSON(baseItem.toJSON())
    is DexItem.UndiscoveredDexItem -> DexItemJSON.UndiscoveredDexItemJSON(id)
}

private fun DexUnits.toJSON(): DexUnitsJSON = DexUnitsJSON(
    discoveredUnits = discoveredUnits.associate { it.ordinal + 1 to it.toJSON() }.toSortedMap(),
    lastUnitId = lastUnitId
)

private fun DexPlants.toJSON(): DexPlantsJSON = DexPlantsJSON(
    discoveredPlants = discoveredPlants.mapValues { it.value.toJSON() }.toSortedMap(),
    lastPlantId = lastPlantId
)
