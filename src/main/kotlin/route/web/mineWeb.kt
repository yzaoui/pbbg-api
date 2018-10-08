package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOrRedirect
import com.bitwiserain.pbbg.memberPageVM
import com.bitwiserain.pbbg.view.page.minePage
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

@Location("/mine")
class MineWebLocation

fun Route.mineWeb(userUC: UserUC) = route("/mine") {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(
            minePage(
                memberPageVM = call.attributes[memberPageVM]
            )
        ) {}
    }
}
