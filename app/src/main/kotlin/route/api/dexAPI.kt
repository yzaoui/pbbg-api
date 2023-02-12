package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.app.domain.model.dex.DexItem
import com.bitwiserain.pbbg.app.domain.model.dex.DexItems
import com.bitwiserain.pbbg.app.domain.model.dex.DexPlants
import com.bitwiserain.pbbg.app.domain.model.dex.DexUnits
import com.bitwiserain.pbbg.app.domain.usecase.DexUC
import com.bitwiserain.pbbg.app.domain.usecase.InvalidItemException
import com.bitwiserain.pbbg.app.domain.usecase.InvalidUnitException
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.serverRootURL
import com.bitwiserain.pbbg.app.user
import com.bitwiserain.pbbg.app.view.model.MyUnitEnumJSON
import com.bitwiserain.pbbg.app.view.model.dex.DexItemJSON
import com.bitwiserain.pbbg.app.view.model.dex.DexItemsJSON
import com.bitwiserain.pbbg.app.view.model.dex.DexPlantsJSON
import com.bitwiserain.pbbg.app.view.model.dex.DexUnitsJSON
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.dexAPI(dexUC: DexUC) = route("/dex") {
    route("/items/{id?}") {
        get {
            val itemEnumId = call.parameters["id"]?.toInt()
            val serverRootURL = call.request.serverRootURL

            if (itemEnumId == null) {
                // Calling for entire item dex
                val dex = dexUC.getDexItems(call.user.id)

                call.respondSuccess(dex.toJSON(serverRootURL = serverRootURL))
            } else {
                // Calling for specific item
                try {
                    val item = dexUC.getIndividualDexBaseItem(call.user.id, itemEnumId)

                    call.respondSuccess(item.toJSON(serverRootURL = serverRootURL))
                } catch (e: InvalidItemException) {
                    call.respondFail(HttpStatusCode.NotFound)
                }
            }
        }
    }

    route("/units/{id?}") {
        get {
            val unitEnumId = call.parameters["id"]?.toInt()
            val serverRootURL = call.request.serverRootURL

            if (unitEnumId == null) {
                // Calling for entire unit dex
                val dex = dexUC.getDexUnits(call.user.id)

                call.respondSuccess(dex.toJSON(serverRootURL = serverRootURL))
            } else {
                // Calling for specific unit
                try {
                    val unit = dexUC.getDexUnit(call.user.id, unitEnumId)

                    call.respondSuccess(unit.toJSON(serverRootURL = serverRootURL))
                } catch (e: InvalidUnitException) {
                    call.respondFail(HttpStatusCode.NotFound)
                }
            }
        }
    }

    route("/plants/{id?}") {
        get {
            val plantId = call.parameters["id"]?.toInt()
            val serverRootURL = call.request.serverRootURL

            if (plantId == null) {
                // Calling for entire plant dex
                val dex = dexUC.getDexPlants(call.user.id)

                call.respondSuccess(dex.toJSON(serverRootURL = serverRootURL))
            } else {
                // Calling for specific plant
                try {
                    val plant = dexUC.getDexPlant(call.user.id, plantId)

                    call.respondSuccess(plant.toJSON(serverRootURL = serverRootURL))
                } catch (e: Exception) {
                    call.respondFail(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

// TODO: Find appropriate place for this adapter
fun MyUnitEnum.toJSON(serverRootURL: String) = MyUnitEnumJSON(
    id = ordinal + 1,
    friendlyName = friendlyName,
    description = description,
    fullURL = "$serverRootURL/img/unit/$spriteName.gif",
    iconURL = "$serverRootURL/img/unit-icon/$spriteName.png",
    baseHP = baseHP,
    baseAtk = baseAtk,
    baseDef = baseDef,
    baseInt = baseInt,
    baseRes = baseRes
)

private fun DexItems.toJSON(serverRootURL: String): DexItemsJSON = DexItemsJSON(
    discoveredItems = discoveredItems.associate { it.ordinal + 1 to it.baseItem.toJSON(serverRootURL = serverRootURL) }.toSortedMap(),
    lastItemId = lastItemId
)

private fun DexItem.toJSON(serverRootURL: String): DexItemJSON = when (this) {
    is DexItem.DiscoveredDexItem -> DexItemJSON.DiscoveredDexItemJSON(baseItem.toJSON(serverRootURL = serverRootURL))
    is DexItem.UndiscoveredDexItem -> DexItemJSON.UndiscoveredDexItemJSON(id)
}

private fun DexUnits.toJSON(serverRootURL: String): DexUnitsJSON = DexUnitsJSON(
    discoveredUnits = discoveredUnits.associate { it.ordinal + 1 to it.toJSON(serverRootURL = serverRootURL) }.toSortedMap(),
    lastUnitId = lastUnitId
)

private fun DexPlants.toJSON(serverRootURL: String): DexPlantsJSON = DexPlantsJSON(
    discoveredPlants = discoveredPlants.mapValues { it.value.toJSON(serverRootURL = serverRootURL) }.toSortedMap(),
    lastPlantId = lastPlantId
)
