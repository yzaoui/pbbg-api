package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.Equippable
import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.Stackable
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.EquipmentJSON
import com.bitwiserain.pbbg.view.model.inventory.InventoryJSON
import com.bitwiserain.pbbg.view.model.ItemJSON
import com.bitwiserain.pbbg.view.model.inventory.InventoryItemJSON
import io.ktor.application.call
import io.ktor.routing.*

fun Route.inventoryAPI(userUC: UserUC, inventoryUC: InventoryUC) = route("/inventory") {
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
        param("action", "equip") {
            post {
                call.respondSuccess("TODO: Equip route")
            }
        }
        param("action", "unequip") {
            post {
                call.respondSuccess("TODO: Unequip route")
            }
        }
    }
}

// TODO: Find appropriate place for this adapter
fun Item.toJSON() = ItemJSON(
    baseId = this.enum.ordinal,
    friendlyName = friendlyName,
    imgURL = "/img/item/$spriteName-64.png",
    quantity = if (this is Stackable) quantity else null,
    description = description,
    equipped = if (this is Equippable) equipped else null
)
