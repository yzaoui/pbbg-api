package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.itemdetails.ItemDetails
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.usecase.ItemUC
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.itemdetails.ItemDetailsJSON
import com.bitwiserain.pbbg.view.model.itemdetails.ItemHistoryJSON
import com.bitwiserain.pbbg.view.model.itemdetails.ItemHistoryInfoJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

private const val ITEM_ID_PARAM = "id"

fun Route.item(itemUC: ItemUC) = route("/item/{$ITEM_ID_PARAM}") {
    get {
        val itemId = call.parameters[ITEM_ID_PARAM]?.toLongOrNull() ?: throw Exception() //TODO: Exception

        val itemdetails = itemUC.getItemDetails(itemId)

        call.respondSuccess(itemdetails.toJSON(itemId))
    }
}

fun ItemDetails.toJSON(itemId: Long) = ItemDetailsJSON(
    item = item.toJSON(itemId),
    history = history.map { it.toJSON() }
)

fun ItemHistory.toJSON() = ItemHistoryJSON(
    date = date.toEpochMilli(),
    info = info.toJSON()
)

fun ItemHistoryInfo.toJSON() = ItemHistoryInfoJSON(
    type = when (this) {
        is ItemHistoryInfo.CreatedInMarket -> "created-market"
        is ItemHistoryInfo.CreatedWithUser -> "created-user"
        is ItemHistoryInfo.Mined -> "mined"
    },
    user = when (this) {
        is ItemHistoryInfo.CreatedInMarket -> null
        is ItemHistoryInfo.CreatedWithUser -> user.toJSON()
        is ItemHistoryInfo.Mined -> user.toJSON()
    }
)

fun ItemHistoryInfo.UserInfo.toJSON() = ItemHistoryInfoJSON.UserInfoJSON(
    id = id,
    name = name
)
