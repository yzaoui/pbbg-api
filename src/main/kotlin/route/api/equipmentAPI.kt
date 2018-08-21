package miner.route.api

import com.google.gson.annotations.SerializedName
import data.model.Pickaxe
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import miner.data.model.Item
import miner.domain.usecase.EquipmentUC
import miner.domain.usecase.UserUC
import miner.interceptSetUserOr401
import miner.loggedInUserKey

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

fun Pickaxe.toItem() = when (this) {
    Pickaxe.PLUS -> Item.PLUS_PICKAXE
    Pickaxe.CROSS -> Item.CROSS_PICKAXE
    Pickaxe.SQUARE -> Item.SQUARE_PICKAXE
}

fun Item.toJSON() = ItemJSON(
    typeId = ordinal,
    friendlyName = friendlyName,
    imgURL = "/img/item/$spriteName.png"
)
