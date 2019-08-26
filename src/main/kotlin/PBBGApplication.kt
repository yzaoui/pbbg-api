package com.bitwiserain.pbbg

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.db.repository.mine.MineSessionTable
import com.bitwiserain.pbbg.db.usecase.*
import com.bitwiserain.pbbg.domain.usecase.*
import com.bitwiserain.pbbg.route.api.*
import com.bitwiserain.pbbg.route.web.*
import com.bitwiserain.pbbg.view.template.GuestPageVM
import com.bitwiserain.pbbg.view.template.MemberPageVM
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.locations.Locations
import io.ktor.locations.locations
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
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

data class ApplicationSession(val userId: Int)
enum class ApplicationEnvironment {
    DEV,
    PROD
}
val loggedInUserKey = AttributeKey<User>("loggedInUser")
val memberPageVM = AttributeKey<MemberPageVM>("memberPageVM")
lateinit var appEnvironment: ApplicationEnvironment

fun Application.main() {
    appEnvironment = when (environment.config.property("ktor.environment").getString()) {
        "dev" -> ApplicationEnvironment.DEV
        "prod" -> ApplicationEnvironment.PROD
        else -> throw RuntimeException("Environment (KTOR_ENV) must be either dev or prod.")
    }

    /*************
     * Set up db *
     *************/
    val jdbcAddress = environment.config.property("jdbc.address").getString()

    val db = Database.connect("jdbc:$jdbcAddress", when {
        jdbcAddress.startsWith("h2:") -> org.h2.Driver::class.qualifiedName!!
        jdbcAddress.startsWith("postgresql:") -> org.h2.Driver::class.qualifiedName!!
        else -> throw RuntimeException("Only H2 and PostgreSQL databases are currently supported.")
    })

    transaction {
        addLogger(Slf4jSqlDebugLogger)
        SchemaUtils.create(
            UserTable, MineSessionTable, MineCellTable, EquipmentTable, InventoryTable, UserStatsTable,
            UnitTable, SquadTable, BattleSessionTable, BattleEnemyTable, DexTable
        )
    }

    install(CallLogging)

    val userUC = UserUCImpl(db)
    val inventoryUC = InventoryUCImpl(db)
    val miningUC = MiningUCImpl(db, inventoryUC)
    val equipmentUC = EquipmentUCImpl(db)
    val unitUC = UnitUCImpl(db)
    val battleUC = BattleUCImpl(db)
    val dexUC = DexUCImpl(db)

    mainWithDependencies(userUC, inventoryUC, miningUC, equipmentUC, unitUC, battleUC, dexUC)
}

fun Application.mainWithDependencies(userUC: UserUC, inventoryUC: InventoryUC, miningUC: MiningUC, equipmentUC: EquipmentUC, unitUC: UnitUC, battleUC: BattleUC, dexUC: DexUC) {
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
    install(Authentication) {
        jwt {
            realm = environment.config.property("jwt.realm").getString()
            verifier(JWT
                .require(Algorithm.HMAC256(environment.config.property("jwt.secret").getString()))
                .withIssuer(environment.config.property("jwt.issuer").getString())
                .build()
            )
            validate {
                it.payload.getClaim("user.id").asInt()?.let(userUC::getUserById)
            }
        }
    }
    routing {
        index(userUC)
        login(userUC)
        logout()
        register(userUC)
        battleWeb(userUC)
        settings(userUC)
        route("/api") {
            registerAPI(userUC)
            loginAPI(userUC)
            authenticate {
                user(userUC)
                inventoryAPI(inventoryUC, equipmentUC)
                mine(miningUC)
                dexAPI(userUC, dexUC)
                squadAPI(unitUC)
            }
            pickaxe(userUC, equipmentUC)
            battleAPI(userUC, battleUC)
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
        static("audio") {
            resources("audio")
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
        battleUrl = href(BattleLocation()),
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

suspend inline fun ApplicationCall.respondSuccess(data: Any? = null, status: HttpStatusCode = HttpStatusCode.OK) {
    respond(status, mapOf("status" to "success", "data" to data))
}

suspend inline fun ApplicationCall.respondFail(data: Any? = null, status: HttpStatusCode = HttpStatusCode.BadRequest) {
    respond(status, mapOf("status" to "fail", "data" to data))
}

suspend inline fun ApplicationCall.respondError(message: String = "", status: HttpStatusCode = HttpStatusCode.InternalServerError) {
    respond(status, mapOf("status" to "error", "message" to message))
}

val ApplicationCall.user
    get() = authentication.principal<User>()!!

fun Application.makeToken(userId: Int) = JWT.create()
    .withIssuer(environment.config.property("jwt.issuer").getString())
    .withClaim("user.id", userId)
    .sign(Algorithm.HMAC256(environment.config.property("jwt.secret").getString()))
