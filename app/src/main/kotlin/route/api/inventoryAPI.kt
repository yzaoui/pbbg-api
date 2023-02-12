package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.model.BaseItem
import com.bitwiserain.pbbg.app.domain.model.Inventory
import com.bitwiserain.pbbg.app.domain.model.InventoryItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import com.bitwiserain.pbbg.app.domain.usecase.EquipmentUC
import com.bitwiserain.pbbg.app.domain.usecase.InventoryFilter.PLANTABLE
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemAlreadyEquippedException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotEquippableException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotEquippedException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotFoundException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryUC
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.serverRootURL
import com.bitwiserain.pbbg.app.user
import com.bitwiserain.pbbg.app.view.model.BaseItemJSON
import com.bitwiserain.pbbg.app.view.model.EquipmentJSON
import com.bitwiserain.pbbg.app.view.model.MaterializedItemJSON
import com.bitwiserain.pbbg.app.view.model.PointJSON
import com.bitwiserain.pbbg.app.view.model.inventory.InventoryItemJSON
import com.bitwiserain.pbbg.app.view.model.inventory.InventoryJSON
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.optionalParam
import io.ktor.server.routing.param
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

fun Route.inventoryAPI(inventoryUC: InventoryUC, equipmentUC: EquipmentUC) = route("/inventory") {
    /**
     * On success:
     *   [InventoryJSON]
     */
    optionalParam("filter") {
        get {
            val filter = when (call.request.queryParameters["filter"]) {
                "plantable" -> PLANTABLE
                null -> null
                else -> throw Exception()
            }

            val inventory = inventoryUC.getInventory(call.user.id, filter)

            call.respondSuccess(inventory.toJSON(serverRootURL = call.request.serverRootURL))
        }
    }

    route("/equipment") {
        param("action") {
            /**
             * Expects query string:
             *   action = equip | unequip
             *
             * Expects body:
             *   [EquipmentActionParams]
             *
             * On success:
             *   [InventoryJSON]
             *
             * Error situations:
             *   [InventoryItemNotFoundException] Must have a pickaxe equipped to mine.
             *   [InventoryItemNotEquippableException] Must be in a mine to mine.
             *   [InventoryItemAlreadyEquippedException] Redundant action if item is already equipped
             *   [InventoryItemNotEquippedException] Redundant action if item is already unequipped
             */
            post {
                try {
                    val equipAction = call.request.queryParameters["action"]

                    if (!(equipAction == "equip" || equipAction == "unequip")) {
                        return@post call.respondFail("Missing equip query parameter")
                    }

                    val body = call.receive<EquipmentActionParams>()
                    val serverRootURL = call.request.serverRootURL

                    when (equipAction) {
                        "equip" -> {
                            equipmentUC.equip(call.user.id, body.inventoryItemId)

                            call.respondSuccess(inventoryUC.getInventory(call.user.id).toJSON(serverRootURL = serverRootURL))
                        }
                        "unequip" -> {
                            equipmentUC.unequip(call.user.id, body.inventoryItemId)

                            call.respondSuccess(inventoryUC.getInventory(call.user.id).toJSON(serverRootURL = serverRootURL))
                        }
                    }
                } catch (e: InventoryItemNotFoundException) {
                    call.respondFail("Item(id=${e.itemId}) can't be found in your inventory.")
                } catch (e: InventoryItemNotEquippableException) {
                    call.respondFail("Item(id=${e.itemId}) is not equippable.")
                } catch (e: InventoryItemAlreadyEquippedException) {
                    call.respondFail("Item(id=${e.itemId}) is already equipped.")
                } catch (e: InventoryItemNotEquippedException) {
                    call.respondFail("Item(id=${e.itemId}) is not equipped.")
                }
            }
        }
    }
}

@Serializable
private data class EquipmentActionParams(
    val inventoryItemId: Long
)

private fun Inventory.toJSON(serverRootURL: String) = InventoryJSON(
    items = items.map { it.toJSON(serverRootURL = serverRootURL) },
    equipment = EquipmentJSON(pickaxe = items
        .filter { it.value.base is BaseItem.Pickaxe && (it.value as InventoryItem.Equippable).equipped }
        .entries.singleOrNull()?.toJSON(serverRootURL = serverRootURL))
)

private fun Map.Entry<Long, InventoryItem>.toJSON(serverRootURL: String): InventoryItemJSON {
    val invItem = value
    val matItem = value.item
    val equipped = if (invItem is InventoryItem.Equippable) invItem.equipped else null

    return InventoryItemJSON(matItem.toJSON(key, serverRootURL = serverRootURL), equipped)
}

// TODO: Find appropriate place for this adapter
fun MaterializedItem.toJSON(id: Long, serverRootURL: String) = MaterializedItemJSON(
    id = id,
    baseItem = this.base.toJSON(serverRootURL = serverRootURL),
    quantity = if (this is MaterializedItem.Stackable) quantity else null
)

fun BaseItem.toJSON(serverRootURL: String) = BaseItemJSON(
    id = id,
    friendlyName = friendlyName,
    img16 = "$serverRootURL/img/item/$spriteName-16.png",
    img32 = "$serverRootURL/img/item/$spriteName-32.png",
    img64 = "$serverRootURL/img/item/$spriteName-64.png",
    description = description,
    grid = if (this is BaseItem.GridPreviewable) grid.map { PointJSON(it.x, it.y) }.toSet() else null
)
