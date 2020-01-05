package com.bitwiserain.pbbg.test.integration.api

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.handleRequest

fun TestApplicationEngine.GETInventory(token: String): TestApplicationResponse =
    handleRequest(HttpMethod.Get, "/api/inventory") {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
    }.response
