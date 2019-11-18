package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.API_ROOT
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.Inventory
import com.bitwiserain.pbbg.domain.model.InventoryItem
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.usecase.*
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.BaseItemJSON
import com.bitwiserain.pbbg.view.model.EquipmentJSON
import com.bitwiserain.pbbg.view.model.MaterializedItemJSON
import com.bitwiserain.pbbg.view.model.PointJSON
import com.bitwiserain.pbbg.view.model.inventory.InventoryItemJSON
import com.bitwiserain.pbbg.view.model.inventory.InventoryJSON
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.*

fun Route.inventoryAPI(inventoryUC: InventoryUC, equipmentUC: EquipmentUC) = route("/inventory") {
    /**
     * On success:
     *   [InventoryJSON]
     */
    get {
        val loggedInUser = call.user

        val inventory = inventoryUC.getInventory(loggedInUser.id)

        call.respondSuccess(inventory.toJSON())
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
             *   [InventoryItemNotEquippable] Must be in a mine to mine.
             *   [InventoryItemAlreadyEquipped] Redundant action if item is already equipped
             *   [InventoryItemNotEquipped] Redundant action if item is already unequipped
             */
            post {
                try {
                    val equipAction = call.parameters["action"]

                    if (!(equipAction == "equip" || equipAction == "unequip")) {
                        return@post call.respondFail("Missing equip query parameter")
                    }

                    val loggedInUser = call.user

                    val body = call.receive<EquipmentActionParams>()

                    when (equipAction) {
                        "equip" -> {
                            equipmentUC.equip(loggedInUser.id, body.inventoryItemId)

                            call.respondSuccess(inventoryUC.getInventory(loggedInUser.id).toJSON())
                        }
                        "unequip" -> {
                            equipmentUC.unequip(loggedInUser.id, body.inventoryItemId)

                            call.respondSuccess(inventoryUC.getInventory(loggedInUser.id).toJSON())
                        }
                    }
                } catch (e: InventoryItemNotFoundException) {
                    call.respondFail("Item(id=${e.itemId}) can't be found in your inventory.")
                } catch (e: InventoryItemNotEquippable) {
                    call.respondFail("Item(id=${e.itemId}) is not equippable.")
                } catch (e: InventoryItemAlreadyEquipped) {
                    call.respondFail("Item(id=${e.itemId}) is already equipped.")
                } catch (e: InventoryItemNotEquipped) {
                    call.respondFail("Item(id=${e.itemId}) is not equipped.")
                }
            }
        }
    }
}

private data class EquipmentActionParams(
    val inventoryItemId: Long
)

private fun Inventory.toJSON() = InventoryJSON(
    items = items.map { it.toJSON() },
    equipment = EquipmentJSON(pickaxe = items
        .filter { it.value.base is BaseItem.Pickaxe && (it.value as InventoryItem.Equippable).equipped }
        .entries.singleOrNull()?.toJSON())
)

private fun Map.Entry<Long, InventoryItem>.toJSON(): InventoryItemJSON {
    val invItem = value
    val matItem = value.item
    val equipped = if (invItem is InventoryItem.Equippable) invItem.equipped else null

    return InventoryItemJSON(matItem.toJSON(key), equipped)
}

// TODO: Find appropriate place for this adapter
fun MaterializedItem.toJSON(id: Long) = MaterializedItemJSON(
    id = id,
    baseItem = this.base.toJSON(),
    quantity = if (this is MaterializedItem.Stackable) quantity else null
)

fun BaseItem.toJSON() = BaseItemJSON(
    friendlyName = friendlyName,
    img16 = "$API_ROOT/img/item/$spriteName-16.png",
    img32 = "$API_ROOT/img/item/$spriteName-32.png",
    img64 = "$API_ROOT/img/item/$spriteName-64.png",
    description = description,
    grid = if (this is BaseItem.GridPreviewable) grid.map { PointJSON(it.x, it.y) }.toSet() else null
)
