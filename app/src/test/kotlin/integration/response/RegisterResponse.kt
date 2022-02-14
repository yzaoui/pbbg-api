package com.bitwiserain.pbbg.app.test.integration.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    @SerialName("token") val token: String
)
