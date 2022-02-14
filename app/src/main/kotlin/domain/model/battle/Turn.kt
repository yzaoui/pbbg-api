package com.bitwiserain.pbbg.app.domain.model.battle

import kotlinx.serialization.Serializable

@Serializable
data class Turn(val unitId: Long, val counter: Int)
