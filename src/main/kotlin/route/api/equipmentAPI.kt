package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.usecase.EquipmentUC
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.google.gson.annotations.SerializedName
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
        val pickaxe = equipmentUC.getPickaxe(loggedInUser.id)

        call.respond(EquipmentJSON(pickaxe?.let { it.toItem().toJSON() } ))
    }
}

data class EquipmentJSON(
    @SerializedName("pickaxe") val pickaxe: ItemJSON?
)
data class ItemJSON(
    @SerializedName("typeId") val typeId: Int,
    @SerializedName("friendlyName") val friendlyName: String,
    @SerializedName("imgURL") val imgURL: String
)

fun Item.toJSON() = ItemJSON(
    typeId = ordinal,
    friendlyName = friendlyName,
    imgURL = "/img/item/$spriteName.png"
)
