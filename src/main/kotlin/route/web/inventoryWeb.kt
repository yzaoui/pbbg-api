package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOrRedirect
import com.bitwiserain.pbbg.memberPageVM
import com.bitwiserain.pbbg.view.page.inventoryPage
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

@Location("/inventory")
class InventoryLocation

fun Route.inventoryWeb(userUC: UserUC) = route("/inventory") {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(
            inventoryPage(
                memberPageVM = call.attributes[memberPageVM]
            )
        ) {}
    }
}
