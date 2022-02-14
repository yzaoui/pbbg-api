package com.bitwiserain.pbbg.app.domain.model.battle

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class BattleQueue(val turns: List<Turn>) {
    fun peek(): Long {
        return turns.first().unitId
    }

    fun endTurn(unitsToRemove: List<Long>): BattleQueue = BattleQueue(
        turns = turns.plus(turns.first()).drop(1) // Move head to tail
            .filter { it.unitId !in unitsToRemove } // Remove all defeated units
    )

    fun toJSON(): String {
        return Json.encodeToString(ListSerializer(Turn.serializer()), turns)
    }

    companion object {
        fun fromJSON(turnsJson: String): BattleQueue {
            return BattleQueue(Json.decodeFromString(ListSerializer(Turn.serializer()), turnsJson))
        }
    }
}
