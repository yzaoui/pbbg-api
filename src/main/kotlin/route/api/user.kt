package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.MiningExperienceManager
import com.bitwiserain.pbbg.domain.model.LevelProgress
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.respondSuccess
import com.google.gson.annotations.SerializedName
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

@Location("/user")
class UserAPILocation

fun Route.user(userUC: UserUC) = route("/user") {
    interceptSetUserOr401(userUC)

    /**
     * On success:
     *   [UserStatsJSON]
     */
    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        val userStats = userUC.getUserStatsByUserId(loggedInUser.id)

        val response = UserStatsJSON(
            miningLvlProgress = MiningExperienceManager.getLevelProgress(userStats.miningExp).toJSON()
        )

        call.respondSuccess(response)
    }
}

data class UserStatsJSON(
    val miningLvlProgress: LevelProgressJSON
)

data class LevelProgressJSON(
    @SerializedName("level") val level: Int,
    @SerializedName("relativeExp") val relativeExp: Int,
    @SerializedName("relativeExpToNextLevel") val relativeExpToNextLevel: Int
)

fun LevelProgress.toJSON() = LevelProgressJSON(
    level = level,
    relativeExp = relativeExp,
    relativeExpToNextLevel = relativeExpNextLevel
)
