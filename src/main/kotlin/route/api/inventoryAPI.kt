package pbbg.route.api

import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import pbbg.data.model.Inventory
import pbbg.data.model.InventoryItem
import pbbg.domain.usecase.InventoryUC
import pbbg.domain.usecase.UserUC
import pbbg.interceptSetUserOr401
import pbbg.loggedInUserKey
import pbbg.respondSuccess

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
