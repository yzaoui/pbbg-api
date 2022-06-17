package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.FarmingExperienceManager
import com.bitwiserain.pbbg.app.domain.MiningExperienceManager
import com.bitwiserain.pbbg.app.domain.model.LevelProgress
import com.bitwiserain.pbbg.app.domain.usecase.GetUserStatsUC
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.user
import com.bitwiserain.pbbg.app.view.model.LevelProgressJSON
import com.bitwiserain.pbbg.app.view.model.UserStatsJSON
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.userStats(getUserStats: GetUserStatsUC) = route("/user-stats") {
    /**
     * On success:
     *   [UserStatsJSON]
     */
    get {
        val userStats = getUserStats(call.user.id)

        val response = UserStatsJSON(
            username = call.user.username,
            gold = userStats.gold,
            miningLvlProgress = MiningExperienceManager.getLevelProgress(userStats.miningExp).toJSON(),
            farmingLvlProgress = FarmingExperienceManager.getLevelProgress(userStats.farmingExp).toJSON()
        )

        call.respondSuccess(response)
    }
}

fun LevelProgress.toJSON() = LevelProgressJSON(
    level = level,
    relativeExp = relativeExp,
    relativeExpToNextLevel = relativeExpNextLevel
)
