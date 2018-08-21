package miner

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.freemarker.FreeMarker
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.locations.locations
import io.ktor.pipeline.PipelineContext
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.util.AttributeKey
import miner.data.*
import miner.data.model.User
import miner.domain.usecase.*
import miner.route.api.equipmentAPI
import miner.route.api.inventoryAPI
import miner.route.api.mine
import miner.route.api.pickaxe
import miner.route.web.equipmentWeb
import miner.route.web.inventoryWeb
import org.h2.Driver
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import route.web.*

data class ApplicationSession(val userId: Int)
val loggedInUserKey = AttributeKey<User>("loggedInUser")

fun Application.main() {
    Database.connect("jdbc:h2:./testDB", Driver::class.qualifiedName!!)
    transaction {
        addLogger(Slf4jSqlDebugLogger)
        SchemaUtils.create(UserTable, MineSessionTable, MineContentsTable, EquipmentTable, InventoryTable)
    }

    val userUC = UserUCImpl()
    val inventoryUC = InventoryUCImpl()
    val miningUC = MiningUCImpl(inventoryUC)
    val equipmentUC = EquipmentUCImpl(inventoryUC)

    install(CallLogging)
    install(Locations)
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "")
    }
    install(Sessions) {
        cookie<ApplicationSession>("miner_session") {
            cookie.path = "/"
        }
    }
    install(ContentNegotiation) {
        gson {
            serializeNulls()
        }
    }
    routing {
        index(userUC)
        login(userUC)
        logout()
        register(userUC)
        mineWeb(userUC, miningUC)
        equipmentWeb(userUC)
        inventoryWeb(userUC)
        route("/api") {
            pickaxe(userUC, equipmentUC)
            equipmentAPI(userUC, equipmentUC)
            mine(userUC, miningUC)
            inventoryAPI(userUC, inventoryUC)
        }
        static("css") {
            resources("css")
        }
        static("js") {
            resources("js")
        }
        static("img") {
            resources("img")
        }
    }
}

/**
 * Ktor-related extensions
 */

fun PipelineContext<Unit, ApplicationCall>.getUserUsingSession(userUC: UserUC): User? {
    return call.sessions.get<ApplicationSession>()?.let { userUC.getUserById(it.userId) }
}

fun Route.interceptSetUserOrRedirect(userUC: UserUC) {
    intercept(ApplicationCallPipeline.Infrastructure) {
        val user = getUserUsingSession(userUC)
        if (user == null) {
            call.respondRedirect(href(LoginLocation()))
            finish()
        } else {
            call.attributes.put(loggedInUserKey, user)
        }
    }
}

fun Route.interceptSetUserOr401(userUC: UserUC) {
    intercept(ApplicationCallPipeline.Infrastructure) {
        val user = getUserUsingSession(userUC)
        if (user == null) {
            call.respondFail(HttpStatusCode.Unauthorized)
            finish()
        } else {
            call.attributes.put(loggedInUserKey, user)
        }
    }
}

fun Route.interceptGuestOnly(userUC: UserUC) {
    intercept(ApplicationCallPipeline.Infrastructure) {
        if (getUserUsingSession(userUC) != null) {
            call.respondRedirect(href(IndexLocation()))
            finish()
        }
    }
}

fun Route.href(location: Any) = application.locations.href(location)

fun PipelineContext<Unit, ApplicationCall>.href(location: Any) = application.locations.href(location)

suspend inline fun ApplicationCall.respondSuccess(data: Any? = null) {
    respond(mapOf("status" to "success", "data" to data))
}

suspend inline fun ApplicationCall.respondSuccess(status: HttpStatusCode, data: Any? = null) {
    respond(status, mapOf("status" to "success", "data" to data))
}

suspend inline fun ApplicationCall.respondFail(data: Any? = null) {
    respond(mapOf("status" to "fail", "data" to data))
}

suspend inline fun ApplicationCall.respondFail(status: HttpStatusCode, data: Any? = null) {
    respond(status, mapOf("status" to "fail", "data" to data))
}

suspend inline fun ApplicationCall.respondError(message: String = "") {
    respond(mapOf("status" to "error", "message" to message))
}

suspend inline fun ApplicationCall.respondError(status: HttpStatusCode, message: String = "") {
    respond(status, mapOf("status" to "error", "message" to message))
}
