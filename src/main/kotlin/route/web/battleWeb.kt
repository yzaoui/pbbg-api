package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOrRedirect
import com.bitwiserain.pbbg.memberPageVM
import com.bitwiserain.pbbg.view.page.battlePage
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

const val BATTLE_PATH = "/battle"
@Location(BATTLE_PATH)
class BattleLocation

fun Route.battleWeb(userUC: UserUC) = route(BATTLE_PATH) {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(battlePage(memberPageVM = call.attributes[memberPageVM])) {}
    }
}
