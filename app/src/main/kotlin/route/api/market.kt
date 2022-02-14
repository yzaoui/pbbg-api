package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.model.market.Market
import com.bitwiserain.pbbg.app.domain.model.market.MarketOrder
import com.bitwiserain.pbbg.app.domain.model.market.UserAndGameMarkets
import com.bitwiserain.pbbg.app.domain.usecase.MarketUC
import com.bitwiserain.pbbg.app.domain.usecase.NotEnoughGoldException
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.user
import com.bitwiserain.pbbg.app.view.model.market.MarketItemJSON
import com.bitwiserain.pbbg.app.view.model.market.MarketJSON
import com.bitwiserain.pbbg.app.view.model.market.UserAndGameMarketsJSON
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.market(marketUC: MarketUC) = route("/market") {
    /**
     * On success:
     *   [UserAndGameMarketsJSON]
     */
    get {
        val markets = marketUC.getMarkets(call.user.id)

        call.respondSuccess(markets.toJSON())
    }

    /**
     * Expects body:
     *   [MarketOrderListParams]
     *
     * On success:
     *   [UserAndGameMarketsJSON]
     */
    post("/buy") {
        try {
            val params = call.receive<MarketOrderListParams>()

            val markets = marketUC.buy(call.user.id, params.orders.map { MarketOrder(it.id, it.quantity) })

            call.respondSuccess(markets.toJSON())
        } catch (e: NotEnoughGoldException) {
            call.respondFail(mapOf("message" to "Not enough gold to make this transaction."))
        }
    }

    /**
     * Expects body:
     *   [MarketOrderListParams]
     *
     * On success:
     *   [UserAndGameMarketsJSON]
     */
    post("/sell") {
        val params = call.receive<MarketOrderListParams>()

        val markets = marketUC.sell(call.user.id, params.orders.map { MarketOrder(it.id, it.quantity) })

        call.respondSuccess(markets.toJSON())
    }
}

private data class MarketOrderListParams(val orders: List<MarketOrderParams>)
private data class MarketOrderParams(val id: Long, val quantity: Int?)

fun Market.toJSON() = MarketJSON(
    items = items.map { MarketItemJSON(it.value.item.toJSON(it.key), it.value.price) }
)

fun UserAndGameMarkets.toJSON() = UserAndGameMarketsJSON(
    gold = gold,
    userMarket = userMarket.toJSON(),
    gameMarket = gameMarket.toJSON()
)
