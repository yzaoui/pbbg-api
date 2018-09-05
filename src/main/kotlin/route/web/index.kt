package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.getMemberPageVM
import com.bitwiserain.pbbg.getUserUsingSession
import com.bitwiserain.pbbg.href
import com.bitwiserain.pbbg.view.GuestPageVM
import com.bitwiserain.pbbg.view.page.homeGuestPage
import com.bitwiserain.pbbg.view.page.homeMemberPage
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

@Location("/")
class IndexLocation

fun Route.index(userUC: UserUC) = route("/") {
    get {
        val loggedInUser = getUserUsingSession(userUC)

        if (loggedInUser != null) {
            call.respondHtmlTemplate(
                homeMemberPage(
                    mineURL = href(MineWebLocation()),
                    inventoryURL = href(InventoryLocation()),
                    equipmentURL = href(EquipmentLocation()),
                    memberPageVM = getMemberPageVM(loggedInUser)
                )
            ) {}
        } else {
            call.respondHtmlTemplate(
                homeGuestPage(
                    registerURL = href(RegisterLocation()),
                    loginURL = href(LoginLocation()),
                    guestPageVM = GuestPageVM()
                )
            ) {}
        }
    }
}
