package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.usecase.AboutUC
import com.bitwiserain.pbbg.app.respondSuccess
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.about(aboutUC: AboutUC) = route("/about") {
    get("/version") {
        call.respondSuccess(aboutUC.getAppVersion())
    }

    get("/patch-notes") {
        call.respondSuccess(aboutUC.getPatchNotes())
    }
}
