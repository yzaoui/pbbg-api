package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.model.mine.Mine
import com.bitwiserain.pbbg.app.domain.model.mine.MineActionResult
import com.bitwiserain.pbbg.app.domain.model.mine.MineEntity
import com.bitwiserain.pbbg.app.domain.model.mine.MineType
import com.bitwiserain.pbbg.app.domain.usecase.mine.ExitMine
import com.bitwiserain.pbbg.app.domain.usecase.mine.GenerateMine
import com.bitwiserain.pbbg.app.domain.usecase.mine.GetAvailableMines
import com.bitwiserain.pbbg.app.domain.usecase.mine.GetMine
import com.bitwiserain.pbbg.app.domain.usecase.mine.SubmitMineAction
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.serverRootURL
import com.bitwiserain.pbbg.app.user
import com.bitwiserain.pbbg.app.view.model.LevelUpJSON
import com.bitwiserain.pbbg.app.view.model.mine.MineActionResultJSON
import com.bitwiserain.pbbg.app.view.model.mine.MineEntityJSON
import com.bitwiserain.pbbg.app.view.model.mine.MineJSON
import com.bitwiserain.pbbg.app.view.model.mine.MineTypeJSON
import com.bitwiserain.pbbg.app.view.model.mine.MineTypeListJSON
import com.bitwiserain.pbbg.app.view.model.mine.MinedItemResultJSON
import io.ktor.server.application.call
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class MinePositionParams(val x: Int, val y: Int)

@Serializable
data class MineGenerateParams(val mineTypeId: Int)

fun Route.mine(
    submitMineAction: SubmitMineAction, getMine: GetMine, getAvailableMines: GetAvailableMines, generateMine: GenerateMine, exitMine: ExitMine
) = route("/mine") {
    /**
     * On success:
     *   [MineJSON] When user has a mine in session.
     *   null When user does not have a mine in session.
     */
    get {
        val mine = getMine(call.user.id)

        call.respondSuccess(mine?.toJSON(serverRootURL = call.request.serverRootURL))
    }

    /**
     * Expects body:
     *   [MinePositionParams]
     *
     * On success:
     *   [MineActionResultJSON]
     */
    post("/perform") {
        val (x: Int, y: Int) = try {
            call.receive<MinePositionParams>()
        } catch (e: ContentTransformationException) {
            return@post call.respondFail("Missing or invalid parameters.")
        }

        val result = submitMineAction(call.user.id, x, y)

        when (result) {
            is SubmitMineAction.Result.Success -> {
                call.respondSuccess(result.data.toJSON(serverRootURL = call.request.serverRootURL))
            }
            SubmitMineAction.Result.NoEquippedPickaxe -> {
                call.respondFail("A pickaxe must be equipped in order to perform a mining operation.")
            }
            SubmitMineAction.Result.NotInMineSession -> {
                call.respondFail("A mine session must be in progress in order to perform a mining operation.")
            }
        }
    }

    /**
     * Expects body:
     *   [MineGenerateParams]
     *
     * On success:
     *   [MineJSON]
     */
    post("/generate") {
        val (mineTypeId: Int) = call.receive<MineGenerateParams>()

        val result = generateMine(call.user.id, mineTypeId, 30, 20)

        when (result) {
            is GenerateMine.Result.SuccessfullyGenerated -> {
                call.respondSuccess(result.mine.toJSON(serverRootURL = call.request.serverRootURL))
            }
            GenerateMine.Result.AlreadyInMine -> {
                call.respondFail("Already in a mine.")
            }
            GenerateMine.Result.InvalidMineTypeId -> {
                call.respondFail("There is no mine with ID: $mineTypeId.")
            }
            is GenerateMine.Result.UnfulfilledLevelRequirement -> {
                call.respondFail("Current mining level (level ${result.currentLevel}) does not meet minimum mining level requirement (level ${result.requiredMinimumLevel}) to generate this type of mine.")
            }
        }
    }

    /**
     * On success:
     *   null
     */
    post("/exit") {
        exitMine(call.user.id)

        call.respondSuccess()
    }

    /**
     * On success:
     *   [MineTypeListJSON]
     */
    get("/types") {
        val result = getAvailableMines(call.user.id).let {
            MineTypeListJSON(
                types = it.mines.map { it.toJSON(serverRootURL = call.request.serverRootURL) },
                nextUnlockLevel = it.nextUnlockLevel
            )
        }

        call.respondSuccess(result)
    }
}

// TODO: Find appropriate place for this adapter
private fun Mine.toJSON(serverRootURL: String) = MineJSON(
    width = width,
    height = height,
    cells = List(height) { y -> List(width) { x -> grid[x to y]?.toJSON(serverRootURL = serverRootURL) } },
    type = mineType.toJSON(serverRootURL = serverRootURL)
)

// TODO: Find appropriate place for this adapter
private fun MineEntity.toJSON(serverRootURL: String) = MineEntityJSON(
    name = friendlyName,
    imageURL = "$serverRootURL/img/mine/entity/$spriteName.png"
)

// TODO: Find appropriate place for this adapter
private fun MineActionResult.toJSON(serverRootURL: String) = MineActionResultJSON(
    minedItemResults = minedItemResults.map {
        MinedItemResultJSON(
            item = it.item.toJSON(it.id, serverRootURL = serverRootURL),
            expPerIndividualItem = it.expPerIndividualItem
        )
    },
    levelUps = levelUps.map { LevelUpJSON(it.newLevel, it.additionalMessage) },
    mine = mine.toJSON(serverRootURL = serverRootURL),
    miningLvl = miningLvl.toJSON()
)

private fun MineType.toJSON(serverRootURL: String) = MineTypeJSON(
    id = ordinal,
    name = friendlyName,
    minLevel = minLevel,
    backgroundURL = "$serverRootURL/img/mine/background/${spriteName}.png"
)
