package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemDetails
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.app.domain.usecase.ItemNotFoundException
import com.bitwiserain.pbbg.app.domain.usecase.ItemUC
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.view.model.itemdetails.ItemDetailsJSON
import com.bitwiserain.pbbg.app.view.model.itemdetails.ItemHistoryInfoJSON
import com.bitwiserain.pbbg.app.view.model.itemdetails.ItemHistoryJSON
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

private const val ITEM_ID_PARAM = "id"

fun Route.item(itemUC: ItemUC) = route("/item/{$ITEM_ID_PARAM}") {
    get {
        val itemId = call.parameters[ITEM_ID_PARAM]?.toLongOrNull() ?: return@get call.respondFail()

        try {
            val itemDetails = itemUC.getItemDetails(itemId)

            call.respondSuccess(itemDetails.toJSON(itemId))
        } catch (e: ItemNotFoundException) {
            call.respondFail("Item with this ID does not exist.")
        }
    }
}

fun ItemDetails.toJSON(itemId: Long) = ItemDetailsJSON(
    item = item.toJSON(itemId),
    history = history.map { it.toJSON() },
    linkedUserInfo = linkedUserInfo
)

fun ItemHistory.toJSON() = ItemHistoryJSON(
    date = date.epochSecond,
    info = info.toJSON()
)

fun ItemHistoryInfo.toJSON() = ItemHistoryInfoJSON(
    type = when (this) {
        is ItemHistoryInfo.CreatedInMarket -> "created-market"
        is ItemHistoryInfo.CreatedWithUser -> "created-user"
        is ItemHistoryInfo.FirstMined -> "first-mined"
        is ItemHistoryInfo.FirstHarvested -> "first-harvested"
    },
    userId = when (this) {
        is ItemHistoryInfo.CreatedInMarket -> null
        is ItemHistoryInfo.CreatedWithUser -> userId
        is ItemHistoryInfo.FirstMined -> userId
        is ItemHistoryInfo.FirstHarvested -> userId
    }
)
