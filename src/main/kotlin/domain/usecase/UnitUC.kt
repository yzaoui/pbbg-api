package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Squad

interface UnitUC {
    fun getSquad(userId: Int): Squad
}
