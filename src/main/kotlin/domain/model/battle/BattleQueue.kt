package com.bitwiserain.pbbg.domain.model.battle

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.parseList

class BattleQueue(val turns: List<Turn>) {
    fun peek(): Long {
        return turns.first().unitId
    }

    fun endTurn(unitsToRemove: List<Long>): BattleQueue = BattleQueue(
        turns = turns.plus(turns.first()).drop(1) // Move head to tail
            .filter { it.unitId !in unitsToRemove } // Remove all defeated units
    )

    @UseExperimental(UnstableDefault::class)
    fun toJSON(): String {
        return Json.stringify(Turn.serializer().list, turns)
    }

    companion object {
        @UseExperimental(ImplicitReflectionSerializer::class)
        fun fromJSON(turnsJson: String): BattleQueue {
            return BattleQueue(Json.parseList(turnsJson))
        }
    }
}
