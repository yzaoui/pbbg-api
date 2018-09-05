package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.Inventory
import com.bitwiserain.pbbg.domain.model.InventoryEntry
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.InventoryItemJSON
import com.bitwiserain.pbbg.view.model.InventoryJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.inventoryAPI(userUC: UserUC, inventoryUC: InventoryUC) = route("/inventory") {
    interceptSetUserOr401(userUC)

    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        val inventory = inventoryUC.getInventory(loggedInUser.id)

        call.respondSuccess(inventory.toJSON())
    }
}

// TODO: Find appropriate place for this adapter
private fun Inventory.toJSON() = InventoryJSON(
    inventoryEntries = this.entries.map { it.toJSON() }
)

// TODO: Find appropriate place for this adapter
private fun InventoryEntry.toJSON() = InventoryItemJSON(
    item = item.toJSON(),
    quantity = quantity
)
