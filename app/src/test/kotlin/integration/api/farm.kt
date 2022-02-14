package com.bitwiserain.pbbg.app.test.integration.api

import com.bitwiserain.pbbg.app.test.integration.requestbody.PlantRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun TestApplicationEngine.GETPlots(token: String): TestApplicationResponse =
    handleRequest(HttpMethod.Get, "/api/farm/plots") {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
    }.response

fun TestApplicationEngine.POSTPlant(token: String, req: PlantRequest): TestApplicationResponse =
    handleRequest(HttpMethod.Post, "/api/farm/plant") {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(req))
    }.response
