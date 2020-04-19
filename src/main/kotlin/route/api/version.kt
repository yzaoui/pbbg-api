package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.APP_VERSION
import com.bitwiserain.pbbg.respondSuccess
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.version() = route("/version") {
    get {
        call.respondSuccess(APP_VERSION)
    }
}
