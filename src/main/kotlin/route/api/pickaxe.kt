package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.usecase.EquipmentUC
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.mine.PickaxeJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.pickaxe(userUC: UserUC, equipmentUC: EquipmentUC) = route("/pickaxe") {
    interceptSetUserOr401(userUC)

    /**
     * On success:
     *   [PickaxeJSON] When user has a pickaxe equipped.
     *   null When user does not have a pickaxe equipped.
     */
    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        val pickaxe = equipmentUC.getEquippedPickaxe(loggedInUser.id)

        call.respondSuccess(pickaxe?.toJSON())
    }
}
