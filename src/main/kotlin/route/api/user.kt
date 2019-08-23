package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.MiningExperienceManager
import com.bitwiserain.pbbg.domain.model.LevelProgress
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.LevelProgressJSON
import com.google.gson.annotations.SerializedName
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

@Location("/user")
class UserAPILocation

fun Route.user(userUC: UserUC) = route("/user") {
    /**
     * On success:
     *   [UserDetailsJSON]
     */
    get {
        val loggedInUser = call.user

        val userStats = userUC.getUserStatsByUserId(loggedInUser.id)

        val response = UserDetailsJSON(
            miningLvlProgress = MiningExperienceManager.getLevelProgress(userStats.miningExp).toJSON()
        )

        call.respondSuccess(response)
    }
}

data class UserDetailsJSON(
    @SerializedName("mining") val miningLvlProgress: LevelProgressJSON
)

fun LevelProgress.toJSON() = LevelProgressJSON(
    level = level,
    relativeExp = relativeExp,
    relativeExpToNextLevel = relativeExpNextLevel
)
