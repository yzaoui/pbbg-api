package com.bitwiserain.pbbg

import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.usecase.EquipmentUCImpl
import com.bitwiserain.pbbg.db.usecase.InventoryUCImpl
import com.bitwiserain.pbbg.db.usecase.MiningUCImpl
import com.bitwiserain.pbbg.db.usecase.UserUCImpl
import com.bitwiserain.pbbg.domain.usecase.EquipmentUC
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import com.bitwiserain.pbbg.domain.usecase.MiningUC
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.route.api.inventoryAPI
import com.bitwiserain.pbbg.route.api.mine
import com.bitwiserain.pbbg.route.api.pickaxe
import com.bitwiserain.pbbg.route.api.user
import com.bitwiserain.pbbg.route.web.*
import com.bitwiserain.pbbg.view.template.GuestPageVM
import com.bitwiserain.pbbg.view.template.MemberPageVM
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
import org.h2.Driver
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

data class ApplicationSession(val userId: Int)
val loggedInUserKey = AttributeKey<User>("loggedInUser")
val memberPageVM = AttributeKey<MemberPageVM>("memberPageVM")

fun Application.main() {
    val db = Database.connect("jdbc:h2:./testDB", Driver::class.qualifiedName!!)
    transaction {
        addLogger(Slf4jSqlDebugLogger)
        SchemaUtils.create(UserTable, MineSessionTable, MineCellTable, EquipmentTable, InventoryTable, UserStatsTable)
    }

    install(CallLogging)

    val userUC = UserUCImpl(db)
    val inventoryUC = InventoryUCImpl(db)
    val miningUC = MiningUCImpl(db, inventoryUC)
    val equipmentUC = EquipmentUCImpl(db)

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
        // Handles "application/json" content type
        gson {
            serializeNulls()
        }
    }
    routing {
        index(userUC)
        login(userUC)
        logout()
        register(userUC)
        squad(userUC)
        mineWeb(userUC)
        battle(userUC)
        inventoryWeb(userUC)
        settings(userUC)
        route("/api") {
            user(userUC)
            pickaxe(userUC, equipmentUC)
            mine(userUC, miningUC)
            inventoryAPI(userUC, inventoryUC, equipmentUC)
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
        homeUrl = href(IndexLocation()),
        squadUrl = href(SquadLocation()),
        inventoryUrl = href(InventoryLocation()),
        battleUrl = href(BattleLocation()),
        mineUrl = href(MineWebLocation()),
        settingsUrl = href(SettingsLocation()),
        logoutUrl = href(LogoutLocation())
    )
}

fun PipelineContext<Unit, ApplicationCall>.getGuestPageVM(): GuestPageVM {
    return GuestPageVM(
        loginURL = href(LoginLocation())
    )
}

fun Route.interceptSetUserOrRedirect(userUC: UserUC) {
    intercept(ApplicationCallPipeline.Features) {
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
    intercept(ApplicationCallPipeline.Features) {
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
    intercept(ApplicationCallPipeline.Features) {
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
