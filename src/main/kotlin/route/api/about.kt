package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.APP_VERSION
import com.bitwiserain.pbbg.PatchNotesHelper
import com.bitwiserain.pbbg.respondSuccess
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.about() = route("/about") {
    get("/version") {
        call.respondSuccess(APP_VERSION)
    }

    get("/patch-notes") {
        call.respondSuccess(PatchNotesHelper.getAll())
    }
}
