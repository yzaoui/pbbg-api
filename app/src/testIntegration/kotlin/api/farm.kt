package com.bitwiserain.pbbg.app.testintegration.api

import com.bitwiserain.pbbg.app.testintegration.requestbody.PlantRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun ApplicationTestBuilder.GETPlots(token: String): HttpResponse =
    client.get("/api/farm/plots") {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

suspend fun ApplicationTestBuilder.POSTPlant(token: String, req: PlantRequest): HttpResponse =
    client.post("/api/farm/plant") {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(req))
    }
