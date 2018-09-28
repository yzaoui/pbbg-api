package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.EquipmentJSON
import com.bitwiserain.pbbg.view.model.InventoryJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.inventoryAPI(userUC: UserUC, inventoryUC: InventoryUC) = route("/inventory") {
    interceptSetUserOr401(userUC)

    /**
     * Responds with [InventoryJSON]
     */
    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        val inventory = inventoryUC.getInventory(loggedInUser.id)

        call.respondSuccess(InventoryJSON(
            items = inventory.items.map { it.toJSON() },
            equipment = EquipmentJSON(
                pickaxe = inventory.equipment.pickaxe?.toJSON()
            )
        ))
    }
}
