package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.MiningExperienceManager
import com.bitwiserain.pbbg.domain.model.LevelProgress
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.LevelProgressJSON
import com.bitwiserain.pbbg.view.model.UserStatsJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.user(userUC: UserUC) = route("/user") {
    /**
     * On success:
     *   [UserStatsJSON]
     */
    get {
        val loggedInUser = call.user

        val userStats = userUC.getUserStatsByUserId(loggedInUser.id)

        val response = UserStatsJSON(
            username = loggedInUser.username,
            gold = userStats.gold,
            miningLvlProgress = MiningExperienceManager.getLevelProgress(userStats.miningExp).toJSON()
        )

        call.respondSuccess(response)
    }
}

fun LevelProgress.toJSON() = LevelProgressJSON(
    level = level,
    relativeExp = relativeExp,
    relativeExpToNextLevel = relativeExpNextLevel
)
