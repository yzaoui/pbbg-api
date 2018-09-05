package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.domain.usecase.MiningUC
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.href
import com.bitwiserain.pbbg.interceptSetUserOrRedirect
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.memberPageVM
import com.bitwiserain.pbbg.view.page.minePage
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

@Location("/mine")
class MineWebLocation

fun Route.mineWeb(userUC: UserUC, miningUC: MiningUC) = route("/mine") {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(
            minePage(
                memberPageVM = call.attributes[memberPageVM]
            )
        ) {}
    }

    post {
        // TODO: Remove this, move to API
        val loggedInUser = call.attributes[loggedInUserKey]

        miningUC.generateMine(loggedInUser.id, 30, 20)

        call.respondRedirect(href(MineWebLocation()))
    }
}
