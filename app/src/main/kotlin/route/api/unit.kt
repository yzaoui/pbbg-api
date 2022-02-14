package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.usecase.UnitNotFoundException
import com.bitwiserain.pbbg.app.domain.usecase.UnitUC
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

private const val UNIT_ID_PARAM = "id"

fun Route.unit(unitUC: UnitUC) = route("/unit/{$UNIT_ID_PARAM}") {
    get {
        val unitId = call.parameters[UNIT_ID_PARAM]?.toLongOrNull() ?: return@get call.respondFail()

        try {
            val unit = unitUC.getUnit(unitId)

            call.respondSuccess(unit.toJSON())
        } catch (e: UnitNotFoundException) {
            call.respondFail("Unit with this ID does not exist.")
        }
    }
}
