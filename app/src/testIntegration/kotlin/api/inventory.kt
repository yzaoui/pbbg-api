package com.bitwiserain.pbbg.app.testintegration.api

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.ApplicationTestBuilder

suspend fun ApplicationTestBuilder.GETInventory(token: String, filter: String? = null): HttpResponse =
    client.get("/api/inventory" + if (filter != null) "?filter=$filter" else "") {
        header(HttpHeaders.Authorization, "Bearer $token")
    }
