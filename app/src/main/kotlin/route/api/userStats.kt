package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.FarmingExperienceManager
import com.bitwiserain.pbbg.domain.MiningExperienceManager
import com.bitwiserain.pbbg.domain.model.LevelProgress
import com.bitwiserain.pbbg.domain.usecase.GetUserStatsUC
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.LevelProgressJSON
import com.bitwiserain.pbbg.view.model.UserStatsJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

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
