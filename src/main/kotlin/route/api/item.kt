package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.itemdetails.ItemDetails
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.usecase.ItemNotFoundException
import com.bitwiserain.pbbg.domain.usecase.ItemUC
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.itemdetails.ItemDetailsJSON
import com.bitwiserain.pbbg.view.model.itemdetails.ItemHistoryInfoJSON
import com.bitwiserain.pbbg.view.model.itemdetails.ItemHistoryJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

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
