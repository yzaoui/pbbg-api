package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.market.MarketItemJSON
import com.bitwiserain.pbbg.view.model.market.MarketJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.market() = route("/market") {
    /**
     * On success:
     *   [MarketJSON]
     */
    get {
        call.respondSuccess(MarketJSON(
            items = listOf(MarketItemJSON(
                item = MaterializedItem.SquarePickaxe.toJSON(),
                price = 5
            ))
        ))
    }
}
