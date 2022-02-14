package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.domain.model.mine.Mine
import com.bitwiserain.pbbg.domain.model.mine.MineActionResult
import com.bitwiserain.pbbg.domain.model.mine.MineEntity
import com.bitwiserain.pbbg.domain.model.mine.MineType
import com.bitwiserain.pbbg.domain.usecase.*
import com.bitwiserain.pbbg.view.model.LevelUpJSON
import com.bitwiserain.pbbg.view.model.mine.*
import io.ktor.application.call
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

data class MinePositionParams(val x: Int, val y: Int)

data class MineGenerateParams(val mineTypeId: Int)

fun Route.mine(miningUC: MiningUC) = route("/mine") {
    /**
     * On success:
     *   [MineJSON] When user has a mine in session.
     *   null When user does not have a mine in session.
     */
    get {
        val mine = miningUC.getMine(call.user.id)

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
                val (x: Int, y: Int) = call.receive(MinePositionParams::class)

                val mineActionResult = miningUC.submitMineAction(call.user.id, x, y).toJSON()

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

                val mine = miningUC.generateMine(call.user.id, mineTypeId, 30, 20)

                call.respondSuccess(mine.toJSON())
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
            val result = miningUC.getAvailableMines(call.user.id).let {
                MineTypeListJSON(
                    types = it.mines.map { it.toJSON() },
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
    cells = List(height) { y -> List(width) { x -> grid[x to y]?.toJSON() } },
    type = mineType.toJSON()
)

// TODO: Find appropriate place for this adapter
private fun MineEntity.toJSON() = MineEntityJSON(
    name = friendlyName,
    imageURL = "$API_ROOT/img/mine/entity/$spriteName.png"
)

// TODO: Find appropriate place for this adapter
private fun MineActionResult.toJSON() = MineActionResultJSON(
    minedItemResults = minedItemResults.map {
        MinedItemResultJSON(
            item = it.item.toJSON(it.id),
            expPerIndividualItem = it.expPerIndividualItem
        )
    },
    levelUps = levelUps.map { LevelUpJSON(it.newLevel, it.additionalMessage) },
    mine = mine.toJSON(),
    miningLvl = miningLvl.toJSON()
)

private fun MineType.toJSON() = MineTypeJSON(
    id = ordinal,
    name = friendlyName,
    minLevel = minLevel,
    backgroundURL = "$API_ROOT/img/mine/background/${spriteName}.png"
)
