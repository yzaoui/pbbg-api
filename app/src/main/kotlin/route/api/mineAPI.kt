package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.model.mine.Mine
import com.bitwiserain.pbbg.app.domain.model.mine.MineActionResult
import com.bitwiserain.pbbg.app.domain.model.mine.MineEntity
import com.bitwiserain.pbbg.app.domain.model.mine.MineType
import com.bitwiserain.pbbg.app.domain.usecase.MiningUC
import com.bitwiserain.pbbg.app.domain.usecase.NoEquippedPickaxeException
import com.bitwiserain.pbbg.app.domain.usecase.NotInMineSessionException
import com.bitwiserain.pbbg.app.domain.usecase.mine.AlreadyInMineException
import com.bitwiserain.pbbg.app.domain.usecase.mine.GenerateMine
import com.bitwiserain.pbbg.app.domain.usecase.mine.GetAvailableMines
import com.bitwiserain.pbbg.app.domain.usecase.mine.GetMine
import com.bitwiserain.pbbg.app.domain.usecase.mine.InvalidMineTypeIdException
import com.bitwiserain.pbbg.app.domain.usecase.mine.UnfulfilledLevelRequirementException
import com.bitwiserain.pbbg.app.respondError
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
    miningUC: MiningUC, getMine: GetMine, getAvailableMines: GetAvailableMines, generateMine: GenerateMine
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

    route("/perform") {
        /**
         * Expects body:
         *   [MinePositionParams]
         *
         * On success:
         *   [MineActionResultJSON]
         *
         * Error situations:
         *   [NoEquippedPickaxeException] Must have a pickaxe equipped to mine.
         *   [NotInMineSessionException] Must be in a mine to mine.
         */
        post {
            try {
                val (x: Int, y: Int) = call.receive(MinePositionParams::class)

                val mineActionResult = miningUC.submitMineAction(call.user.id, x, y).toJSON(serverRootURL = call.request.serverRootURL)

                call.respondSuccess(mineActionResult)
            } catch (e: ContentTransformationException) {
                call.respondFail("Missing or invalid parameters.")
            } catch (e: NoEquippedPickaxeException) {
                call.respondFail("A pickaxe must be equipped in order to perform a mining operation.")
            } catch (e: NotInMineSessionException) {
                call.respondFail("A mine session must be in progress in order to perform a mining operation.")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondError(e.message.orEmpty())
            }
        }
    }

    route("/generate") {
        /**
         * Expects body:
         *   [MineGenerateParams]
         *
         * On success:
         *   [MineJSON]
         *
         * Error situations:
         *   [AlreadyInMineException] Must not already be in a mine.
         *   [InvalidMineTypeIdException] Mine type ID must be a valid type ID.
         *   [UnfulfilledLevelRequirementException] Must have minimum required level.
         */
        post {
            try {
                val (mineTypeId: Int) = call.receive<MineGenerateParams>()

                val mine = generateMine(call.user.id, mineTypeId, 30, 20)

                call.respondSuccess(mine.toJSON(serverRootURL = call.request.serverRootURL))
            } catch (e: AlreadyInMineException) {
                call.respondFail("Already in a mine.")
            } catch (e: InvalidMineTypeIdException) {
                call.respondFail("There is no mine with ID: ${e.id}.")
            } catch (e: UnfulfilledLevelRequirementException) {
                call.respondFail("Current mining level (level ${e.currentLevel}) does not meet minimum mining level requirement (level ${e.requiredMinimumLevel}) to generate this type of mine.")
            }
        }
    }

    route("/exit") {
        /**
         * On success:
         *   null
         */
        post {
            // TODO: Maybe throw an exception if not currently in a mine
            miningUC.exitMine(call.user.id)

            call.respondSuccess()
        }
    }

    route("/types") {
        /**
         * On success:
         *   [MineTypeListJSON]
         */
        get {
            val result = getAvailableMines(call.user.id).let {
                MineTypeListJSON(
                    types = it.mines.map { it.toJSON(serverRootURL = call.request.serverRootURL) },
                    nextUnlockLevel = it.nextUnlockLevel
                )
            }

            call.respondSuccess(result)
        }
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
