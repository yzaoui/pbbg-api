package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.market.MarketItemJSON
import com.bitwiserain.pbbg.view.model.market.MarketJSON
import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.market(inventoryUC: InventoryUC) = route("/market") {
    /**
     * On success:
     *   [MarketJSON]
     */
    get {
        call.respondSuccess(MarketJSON(
            items = listOf(MarketItemJSON(
                id = 1,
                item = MaterializedItem.SquarePickaxe.toJSON(),
                price = 5
            ))
        ))
    }

    /**
     * On success:
     *   [MarketJSON]
     */
    get("/inventory") {
        val loggedInUser = call.user

        val inventory = inventoryUC.getInventory(loggedInUser.id)

        call.respondSuccess(MarketJSON(
            items = inventory.items.map { MarketItemJSON(
                id = it.key,
                item = it.value.item.toJSON(),
                price = 5
            ) }
        ))
    }
}
