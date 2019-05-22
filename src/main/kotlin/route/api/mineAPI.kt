package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.domain.model.mine.Mine
import com.bitwiserain.pbbg.domain.model.mine.MineActionResult
import com.bitwiserain.pbbg.domain.model.mine.MineEntity
import com.bitwiserain.pbbg.domain.usecase.*
import com.bitwiserain.pbbg.view.model.LevelUpJSON
import com.bitwiserain.pbbg.view.model.mine.*
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

data class MinePositionParams(val x: Int, val y: Int)

data class MineGenerateParams(val mineTypeId: Int)

fun Route.mine(userUC: UserUC, miningUC: MiningUC) = route("/mine") {
    interceptSetUserOr401(userUC)

    /**
     * On success:
     *   [MineJSON] When user has a mine in session.
     *   null When user does not have a mine in session.
     */
    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        val mine = miningUC.getMine(loggedInUser.id)

        call.respondSuccess(mine?.toJSON())
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
                // TODO: Remove cookie dependency
                val loggedInUser = call.attributes[loggedInUserKey]

                val (x: Int, y: Int) = call.receive(MinePositionParams::class)

                val mineActionResult = miningUC.submitMineAction(loggedInUser.id, x, y).toJSON()

                call.respondSuccess(mineActionResult)
            } catch (e: ContentTransformationException) {
                call.respondFail(HttpStatusCode.BadRequest, "Missing or invalid parameters.")
            } catch (e: NoEquippedPickaxeException) {
                call.respondFail(HttpStatusCode.Forbidden, "A pickaxe must be equipped in order to perform a mining operation.")
            } catch (e: NotInMineSessionException) {
                call.respondFail(HttpStatusCode.Forbidden, "A mine session must be in progress in order to perform a mining operation.")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondError(HttpStatusCode.InternalServerError, e.message.orEmpty())
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
         *   [UnfulfilledLevelRequirementException] Must have minimum required level.
         */
        post {
            try {
                val loggedInUser = call.attributes[loggedInUserKey]

                val (mineTypeId: Int) = call.receive<MineGenerateParams>() // TODO: Respond with error on illegal ID

                val mine = miningUC.generateMine(loggedInUser.id, mineTypeId, 30, 20)

                call.respondSuccess(mine.toJSON())
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
            val loggedInUser = call.attributes[loggedInUserKey]

            miningUC.exitMine(loggedInUser.id)

            call.respondSuccess()
        }
    }

    route("/types") {
        /**
         * On success:
         *   [MineTypeListJSON]
         */
        get {
            val loggedInUser = call.attributes[loggedInUserKey]

            val result = miningUC.getAvailableMines(loggedInUser.id).let {
                MineTypeListJSON(
                    types = it.mines.map {
                        MineTypeListJSON.MineTypeJSON(id = it.ordinal, name = it.friendlyName, minLevel = it.minLevel)
                    },
                    nextUnlockLevel = it.nextUnlockLevel
                )
            }

            call.respondSuccess(result)
        }
    }
}

// TODO: Find appropriate place for this adapter
private fun Mine.toJSON() = MineJSON(
    width = width,
    height = height,
    cells = List(height) { y -> List(width) { x -> grid[x to y]?.toJSON() } }
)

// TODO: Find appropriate place for this adapter
private fun MineEntity.toJSON() = MineEntityJSON(
    imageURL = "/img/mine/$spriteName.png"
)

// TODO: Find appropriate place for this adapter
private fun MineActionResult.toJSON() = MineActionResultJSON(
    minedItemResults = minedItemResults.map {
        MinedItemResultJSON(
            item = it.item.toJSON(),
            expPerIndividualItem = it.expPerIndividualItem
        )
    },
    levelUps = levelUps.map { LevelUpJSON(it.newLevel, it.additionalMessage) }
)
