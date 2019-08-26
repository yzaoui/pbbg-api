package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.domain.model.*
import com.bitwiserain.pbbg.domain.usecase.*
import com.bitwiserain.pbbg.view.model.EquipmentJSON
import com.bitwiserain.pbbg.view.model.ItemEnumJSON
import com.bitwiserain.pbbg.view.model.ItemJSON
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
             *   [InventoryItemAlreadyUnequipped] Redundant action if item is already unequipped
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
                    call.respondFail("Item with ID ${e.itemId} can't be found in your inventory.")
                } catch (e: InventoryItemNotEquippable) {
                    call.respondFail("Item with ID ${e.itemId} is not equippable.")
                } catch (e: InventoryItemAlreadyEquipped) {
                    call.respondFail("Item with ID ${e.itemId} is already equipped.")
                } catch (e: InventoryItemAlreadyUnequipped) {
                    call.respondFail("Item with ID ${e.itemId} is already unequipped.")
                }
            }
        }
    }
}

private data class EquipmentActionParams(
    val inventoryItemId: Int
)

private fun Inventory.toJSON() = InventoryJSON(
    items = items.map { InventoryItemJSON(it.key, it.value.toJSON()) },
    equipment = EquipmentJSON(
        pickaxe = equipment.pickaxe?.toJSON()
    )
)

// TODO: Find appropriate place for this adapter
fun Item.toJSON() = ItemJSON(
    baseItem = this.enum.toJSON(),
    quantity = if (this is Stackable) quantity else null,
    equipped = if (this is Equippable) equipped else null,
    grid = if (this is GridPreviewable) grid.map { PointJSON(it.x, it.y) }.toSet() else null
)

// TODO: Find appropriate place for this adapter
fun ItemEnum.toJSON() = ItemEnumJSON(
    friendlyName = friendlyName,
    imgURL = "/img/item/$spriteName-64.png",
    description = description
)
