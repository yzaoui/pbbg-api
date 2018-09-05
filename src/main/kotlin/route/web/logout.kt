package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.ApplicationSession
import com.bitwiserain.pbbg.href
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.sessions.clear
import io.ktor.sessions.sessions

@Location("/logout")
class LogoutLocation

fun Route.logout() = route("/logout") {
    post {
        call.sessions.clear<ApplicationSession>()
        call.respondRedirect(href(IndexLocation()))
    }
}
