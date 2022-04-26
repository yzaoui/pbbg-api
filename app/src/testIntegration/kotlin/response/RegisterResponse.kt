package com.bitwiserain.pbbg.app.testintegration.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    @SerialName("token") val token: String
)
