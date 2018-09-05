package pbbg

import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
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
import pbbg.data.*
import pbbg.data.model.User
import pbbg.domain.usecase.*
import pbbg.route.api.equipmentAPI
import pbbg.route.api.inventoryAPI
import pbbg.route.api.mine
import pbbg.route.api.pickaxe
import pbbg.route.web.equipmentWeb
import pbbg.route.web.inventoryWeb
import pbbg.view.ActionVM
import pbbg.view.MemberPageVM
import org.h2.Driver
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import route.web.*

data class ApplicationSession(val userId: Int)
val loggedInUserKey = AttributeKey<User>("loggedInUser")
val memberPageVM = AttributeKey<MemberPageVM>("memberPageVM")

fun Application.main() {
    val db = Database.connect("jdbc:h2:./testDB", Driver::class.qualifiedName!!)
    transaction {
        addLogger(Slf4jSqlDebugLogger)
        SchemaUtils.create(UserTable, MineSessionTable, MineContentsTable, EquipmentTable, InventoryTable)
    }

    install(CallLogging)

    val userUC = UserUCImpl(db)
    val inventoryUC = InventoryUCImpl(db)
    val miningUC = MiningUCImpl(db, inventoryUC)
    val equipmentUC = EquipmentUCImpl(db, inventoryUC)

    mainWithDependencies(userUC, inventoryUC, miningUC, equipmentUC)
}

fun Application.mainWithDependencies(userUC: UserUC, inventoryUC: InventoryUC, miningUC: MiningUC, equipmentUC: EquipmentUC) {
    install(Sessions) {
        cookie<ApplicationSession>("pbbg_session") {
            cookie.path = "/"
        }
    }
    install(Locations)
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

fun PipelineContext<Unit, ApplicationCall>.getMemberPageVM(user: User): MemberPageVM {
    return MemberPageVM(
        user = user,
        home = ActionVM("Home", href(IndexLocation())),
        logout = ActionVM("Log out", href(LogoutLocation()))
    )
}

fun Route.interceptSetUserOrRedirect(userUC: UserUC) {
    intercept(ApplicationCallPipeline.Infrastructure) {
        val user = getUserUsingSession(userUC)
        if (user == null) {
            call.respondRedirect(href(LoginLocation()))
            finish()
        } else {
            call.attributes.put(loggedInUserKey, user)
            call.attributes.put(memberPageVM, getMemberPageVM(user))
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
