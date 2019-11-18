package com.bitwiserain.pbbg

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.db.repository.market.MarketTable
import com.bitwiserain.pbbg.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.db.repository.mine.MineSessionTable
import com.bitwiserain.pbbg.db.usecase.*
import com.bitwiserain.pbbg.domain.usecase.*
import com.bitwiserain.pbbg.route.api.*
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

enum class ApplicationEnvironment {
    DEV,
    PROD
}
lateinit var appEnvironment: ApplicationEnvironment
lateinit var API_ROOT: String

fun Application.main() {
    appEnvironment = when (environment.config.propertyOrNull("ktor.environment")?.getString()) {
        "dev" -> ApplicationEnvironment.DEV
        "prod" -> ApplicationEnvironment.PROD
        else -> throw RuntimeException("Environment (KTOR_ENV) must be either dev or prod.")
    }

    API_ROOT = when(appEnvironment) {
        ApplicationEnvironment.DEV -> "http://localhost:${environment.config.property("ktor.deployment.port").getString()}"
        ApplicationEnvironment.PROD -> "https://pbbg-api.bitwiserain.com"
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
            UserTable, MineSessionTable, MineCellTable, MaterializedItemTable, InventoryTable, UserStatsTable,
            UnitTable, SquadTable, BattleSessionTable, BattleEnemyTable, DexTable, MarketTable, MarketInventoryTable,
            ItemHistoryTable
        )
    }

    install(CallLogging)

    val userUC = UserUCImpl(db)
    val marketUC = MarketUCImpl(db)
    val inventoryUC = InventoryUCImpl(db)
    val itemUC = ItemUCImpl(db)
    val miningUC = MiningUCImpl(db)
    val equipmentUC = EquipmentUCImpl(db)
    val unitUC = UnitUCImpl(db)
    val battleUC = BattleUCImpl(db)
    val dexUC = DexUCImpl(db)

    mainWithDependencies(userUC, marketUC, itemUC, inventoryUC, miningUC, equipmentUC, unitUC, battleUC, dexUC)
}

fun Application.mainWithDependencies(userUC: UserUC, marketUC: MarketUC, itemUC: ItemUC, inventoryUC: InventoryUC, miningUC: MiningUC, equipmentUC: EquipmentUC, unitUC: UnitUC, battleUC: BattleUC, dexUC: DexUC) {
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
    install(CORS) {
        anyHost()
        header(HttpHeaders.Authorization)
        allowNonSimpleContentTypes = true
    }
    routing {
        route("/api") {
            registerAPI(userUC)
            loginAPI(userUC)
            item(itemUC)
            authenticate {
                user(userUC)
                inventoryAPI(inventoryUC, equipmentUC)
                market(marketUC)
                battleAPI(battleUC)
                mine(miningUC)
                dexAPI(dexUC)
                squadAPI(unitUC)
            }
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
