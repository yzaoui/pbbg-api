package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.dex.DexUnits
import com.bitwiserain.pbbg.domain.usecase.DexUC
import com.bitwiserain.pbbg.domain.usecase.InvalidUnitException
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.MyUnitEnumJSON
import com.bitwiserain.pbbg.view.model.dex.DexItemsJSON
import com.bitwiserain.pbbg.view.model.dex.DexUnitsJSON
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.dexAPI(userUC: UserUC, dexUC: DexUC) = route("/dex") {
    route("/items") {
        get {
            val loggedInUser = call.user

            val dex = dexUC.getDexItems(loggedInUser.id)

            call.respondSuccess(
                DexItemsJSON(
                    discoveredItems = dex.discoveredItems.associate { it.ordinal to it.baseItem.toJSON() }.toSortedMap(),
                    lastItemIsDiscovered = dex.lastItemIsDiscovered
                )
            )
        }
    }

    route("/units/{id?}") {
        get {
            val loggedInUser = call.user

            val unitEnumId = call.parameters["id"]?.toInt()

            if (unitEnumId == null) {
                // Calling for entire unit dex
                val dex = dexUC.getDexUnits(loggedInUser.id)

                call.respondSuccess(dex.toJSON())
            } else {
                // Calling for specific unit
                try {
                    val unit = dexUC.getDexUnit(loggedInUser.id, unitEnumId)

                    call.respondSuccess(unit.toJSON())
                } catch (e: InvalidUnitException) {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

// TODO: Find appropriate place for this adapter
fun MyUnitEnum.toJSON() = MyUnitEnumJSON(
    id = ordinal,
    friendlyName = friendlyName,
    description = description,
    fullURL = "/img/unit/$spriteName.gif",
    iconURL = "/img/unit-icon/$spriteName.png"
)

private fun DexUnits.toJSON() = DexUnitsJSON(
    discoveredUnits = discoveredUnits.associate { it.ordinal to it.toJSON() }.toSortedMap(),
    lastUnitIsDiscovered = lastUnitIsDiscovered
)
