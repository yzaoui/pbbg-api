package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.market.Market
import com.bitwiserain.pbbg.domain.model.market.MarketOrder
import com.bitwiserain.pbbg.domain.usecase.MarketUC
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.market.MarketItemJSON
import com.bitwiserain.pbbg.view.model.market.MarketJSON
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.market(marketUC: MarketUC) = route("/market") {
    /**
     * On success:
     *   [MarketJSON]
     */
    get {
        val loggedInUser = call.user

        val market = marketUC.getMarket(loggedInUser.id)

        call.respondSuccess(market.toJSON())
    }

    /**
     * On success:
     *   [MarketJSON]
     */
    get("/inventory") {
        val loggedInUser = call.user

        val market = marketUC.getUserInventory(loggedInUser.id)

        call.respondSuccess(market.toJSON())
    }

    /**
     * Expects body:
     *   [MarketOrderListParams]
     *
     * On success:
     *   [MarketJSON]
     */
    post("/sell") {
        val loggedInUser = call.user

        val params = call.receive<MarketOrderListParams>()

        val market = marketUC.sell(loggedInUser.id, params.orders.map { MarketOrder(it.id, it.quantity) })

        call.respondSuccess(market.toJSON())
    }
}

private data class MarketOrderListParams(val orders: List<MarketOrderParams>)
private data class MarketOrderParams(val id: Long, val quantity: Int?)

fun Market.toJSON() = MarketJSON(
    items = items.map { MarketItemJSON(it.key, it.value.item.toJSON(), it.value.price) }
)
