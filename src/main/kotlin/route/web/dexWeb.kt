package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOrRedirect
import com.bitwiserain.pbbg.memberPageVM
import com.bitwiserain.pbbg.view.page.dexPage
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

@Location("/dex")
class DexWebLocation

const val PATH = "path"

fun Route.dexWeb(userUC: UserUC) = route("/dex/{$PATH...}") {
    interceptSetUserOrRedirect(userUC)

    /**
     * Responds with 404 if route is not one of:
     *   /dex
     *   /dex/items
     *   /dex/units
     *   /dex/units/{valid dex unit ID}
     */
    intercept(ApplicationCallPipeline.Features) {
        val path = call.parameters.getAll(PATH)!!

        if (!(path.joinToString("/") in listOf("", "items", "units")
                    || (path.size == 2 && path[0] == "units" && path[1].toIntOrNull() in MyUnitEnum.values().indices))
        ) {
            call.respond(HttpStatusCode.NotFound)
            finish()
        }
    }

    get {
        call.respondHtmlTemplate(dexPage(memberPageVM = call.attributes[memberPageVM])) {}
    }
}
