package com.bitwiserain.pbbg.app.test.integration.api

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.handleRequest

fun TestApplicationEngine.GETInventory(token: String, filter: String? = null): TestApplicationResponse =
    handleRequest(HttpMethod.Get, "/api/inventory" + if (filter != null) "?filter=$filter" else "") {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
    }.response
