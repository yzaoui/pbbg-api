package miner.route.api

import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import miner.data.model.Inventory
import miner.data.model.InventoryItem
import miner.domain.usecase.InventoryUC
import miner.domain.usecase.UserUC
import miner.interceptSetUserOr401
import miner.loggedInUserKey
import miner.respondSuccess

fun Route.inventoryAPI(userUC: UserUC, inventoryUC: InventoryUC) = route("/inventory") {
    interceptSetUserOr401(userUC)

    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        val inventory = inventoryUC.getInventory(loggedInUser.id)

        call.respondSuccess(inventory.toJSON())
    }
}

private data class InventoryJSON(val inventoryEntries: List<InventoryItemJSON>)
private fun Inventory.toJSON() = InventoryJSON(
    inventoryEntries = this.items.map { it.toJSON() }
)

private data class InventoryItemJSON(val item: ItemJSON, val quantity: Int)
private fun InventoryItem.toJSON() = InventoryItemJSON(
    item = item.toJSON(),
    quantity = quantity
)
