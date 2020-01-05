package com.bitwiserain.pbbg.test.integration.api

import com.bitwiserain.pbbg.test.integration.requestbody.PlantRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.stringify

fun TestApplicationEngine.GETPlots(token: String): TestApplicationResponse =
    handleRequest(HttpMethod.Get, "/api/farm/plots") {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
    }.response

@ImplicitReflectionSerializer
fun TestApplicationEngine.POSTPlant(token: String, req: PlantRequest): TestApplicationResponse =
    handleRequest(HttpMethod.Post, "/api/farm/plant") {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.stringify(req))
    }.response
