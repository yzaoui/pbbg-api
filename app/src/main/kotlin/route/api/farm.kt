package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.API_ROOT
import com.bitwiserain.pbbg.app.domain.model.farm.IBasePlant
import com.bitwiserain.pbbg.app.domain.model.farm.IMaterializedPlant
import com.bitwiserain.pbbg.app.domain.model.farm.MaterializedPlant
import com.bitwiserain.pbbg.app.domain.model.farm.Plot
import com.bitwiserain.pbbg.app.domain.usecase.FarmUC
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.user
import com.bitwiserain.pbbg.app.view.model.farm.BasePlantJSON
import com.bitwiserain.pbbg.app.view.model.farm.MaterializedPlantJSON
import com.bitwiserain.pbbg.app.view.model.farm.PlotJSON
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
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

    post("/reorder") {
        val now = clock.instant()
        val params = call.receive<ReorderParams>()

        val updatedPlots = farmUC.reorder(call.user.id, params.plotId, params.targetIndex)

        call.respondSuccess(updatedPlots.map { it.toJSON(now) })
    }
}

@Serializable
private data class PlantParams(val plotId: Long, val itemId: Long)

@Serializable
private data class HarvestParams(val plotId: Long)

@Serializable
private data class ReorderParams(val plotId: Long, val targetIndex: Int)

private fun Plot.toJSON(now: Instant) = PlotJSON(
    id = id,
    plant = plant?.run { second.toJSON(now) }
)

private fun MaterializedPlant.toJSON(now: Instant) = MaterializedPlantJSON(
    basePlant = basePlant.toJSON(),
    cycleStart = cycleStart.toString(),
    isMature = (this as? IMaterializedPlant.Maturable)?.isMature(now),
    harvests = (this as? IMaterializedPlant.Maturable)?.harvests
)

fun IBasePlant.toJSON() = BasePlantJSON(
    id = enum.ordinal + 1,
    name = friendlyName,
    description = description,
    icon = "$API_ROOT/img/plant-icon/$spriteName.png",
    growingPeriod = growingPeriod.seconds,
    growingSprite = "$API_ROOT/img/plant/$spriteName-growing.gif",
    maturePeriod = if (this is IBasePlant.Maturable) maturePeriod.seconds else null,
    matureSprite = if (this is IBasePlant.Maturable) "$API_ROOT/img/plant/$spriteName-mature.gif" else null
)
