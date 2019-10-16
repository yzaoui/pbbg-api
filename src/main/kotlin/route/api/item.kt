package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.usecase.ItemUC
import com.bitwiserain.pbbg.respondSuccess
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

private const val ITEM_ID_PARAM = "id"

fun Route.item(itemUC: ItemUC) = route("/item/{$ITEM_ID_PARAM}") {
    get {
        val itemId = call.parameters[ITEM_ID_PARAM]

        call.respondSuccess(itemId)
    }
}
