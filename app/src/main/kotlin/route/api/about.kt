package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.usecase.AboutUC
import com.bitwiserain.pbbg.respondSuccess
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.about(aboutUC: AboutUC) = route("/about") {
    get("/version") {
        call.respondSuccess(aboutUC.getAppVersion())
    }

    get("/patch-notes") {
        call.respondSuccess(aboutUC.getPatchNotes())
    }
}
