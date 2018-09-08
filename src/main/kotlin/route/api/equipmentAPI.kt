package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.Stackable
import com.bitwiserain.pbbg.domain.usecase.EquipmentUC
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.view.model.EquipmentJSON
import com.bitwiserain.pbbg.view.model.ItemJSON
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.equipmentAPI(userUC: UserUC, equipmentUC: EquipmentUC) = route("/equipment") {
    interceptSetUserOr401(userUC)
    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        //TODO: Return whole inventory, not just pickaxe
        val pickaxe = equipmentUC.getEquippedPickaxe(loggedInUser.id)

        call.respond(EquipmentJSON(pickaxe?.let { it.toItem().toJSON() } ))
    }
}

// TODO: Find appropriate place for this adapter
fun Item.toJSON() = ItemJSON(
    baseId = this.enum.ordinal,
    friendlyName = friendlyName,
    imgURL = "/img/item/$spriteName-64.png",
    quantity = if (this is Stackable) quantity else null,
    description = description
)
