package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.Equippable
import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.Stackable
import com.bitwiserain.pbbg.domain.usecase.*
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.EquipmentJSON
import com.bitwiserain.pbbg.view.model.ItemJSON
import com.bitwiserain.pbbg.view.model.inventory.InventoryItemJSON
import com.bitwiserain.pbbg.view.model.inventory.InventoryJSON
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.*

fun Route.inventoryAPI(userUC: UserUC, inventoryUC: InventoryUC, equipmentUC: EquipmentUC) = route("/inventory") {
    interceptSetUserOr401(userUC)

    /**
     * On success:
     *   [InventoryJSON]
     */
    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        val inventory = inventoryUC.getInventory(loggedInUser.id)

        call.respondSuccess(
            InventoryJSON(
                items = inventory.items.map { InventoryItemJSON(it.key, it.value.toJSON()) },
                equipment = EquipmentJSON(
                    pickaxe = inventory.equipment.pickaxe?.toJSON()
                )
            )
        )
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

                    val loggedInUser = call.attributes[loggedInUserKey]

                    val body = call.receive<EquipmentActionParams>()

                    when (equipAction) {
                        "equip" -> {
                            equipmentUC.equip(loggedInUser.id, body.inventoryItemId)

                            call.respondSuccess("Item with ID ${body.inventoryItemId} successfully equipped.")
                        }
                        "unequip" -> {
                            equipmentUC.unequip(loggedInUser.id, body.inventoryItemId)

                            call.respondSuccess("Item with ID ${body.inventoryItemId} successfully unequipped.")
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

data class EquipmentActionParams(
    val inventoryItemId: Int
)

// TODO: Find appropriate place for this adapter
fun Item.toJSON() = ItemJSON(
    baseId = this.enum.ordinal,
    friendlyName = friendlyName,
    imgURL = "/img/item/$spriteName-64.png",
    quantity = if (this is Stackable) quantity else null,
    description = description,
    equipped = if (this is Equippable) equipped else null
)
