package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.API_ROOT
import com.bitwiserain.pbbg.domain.model.farm.IBasePlant
import com.bitwiserain.pbbg.domain.model.farm.IMaterializedPlant
import com.bitwiserain.pbbg.domain.model.farm.MaterializedPlant
import com.bitwiserain.pbbg.domain.model.farm.Plot
import com.bitwiserain.pbbg.domain.usecase.FarmUC
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.farm.BasePlantJSON
import com.bitwiserain.pbbg.view.model.farm.MaterializedPlantJSON
import com.bitwiserain.pbbg.view.model.farm.PlotJSON
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import java.time.Clock
import java.time.Instant

fun Route.farm(farmUC: FarmUC, clock: Clock) = route("/farm") {
    get("/plots") {
        val now = clock.instant()
        val plots = farmUC.getPlots(call.user.id)

        call.respondSuccess(plots.map { it.toJSON(now) })
    }

    post("/plant") {
        val now = clock.instant()
        val params = call.receive<PlantParams>()

        val updatedPlot = farmUC.plant(call.user.id, params.plotId, params.itemId)

        call.respondSuccess(updatedPlot.toJSON(now))
    }

    post("/harvest") {
        val now = clock.instant()
        val params = call.receive<HarvestParams>()

        val updatedPlot = farmUC.harvest(call.user.id, params.plotId)

        call.respondSuccess(updatedPlot.toJSON(now))
    }

    post("/expand") {
        val now = clock.instant()
        val newPlot = farmUC.expand(call.user.id)

        call.respondSuccess(newPlot.toJSON(now))
    }
}

private data class PlantParams(val plotId: Long, val itemId: Long)
private data class HarvestParams(val plotId: Long)

private fun Plot.toJSON(now: Instant) = PlotJSON(
    id = id,
    plant = plant?.run { second.toJSON(now) }
)

private fun MaterializedPlant.toJSON(now: Instant) = MaterializedPlantJSON(
    basePlant = basePlant.toJSON(),
    cycleStart = cycleStart.toString(),
    isMature = if (this is IMaterializedPlant.Maturable) isMature(now) else null
)

private fun IBasePlant.toJSON() = BasePlantJSON(
    name = friendlyName,
    icon = "$API_ROOT/img/plant-icon/$spriteName.png",
    growingPeriod = growingPeriod.seconds,
    growingSprite = "$API_ROOT/img/plant/$spriteName-growing.gif",
    maturePeriod = if (this is IBasePlant.Maturable) maturePeriod.seconds else null,
    matureSprite = if (this is IBasePlant.Maturable) "$API_ROOT/img/plant/$spriteName-mature.gif" else null
)
