package com.bitwiserain.pbbg.app

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.model.User
import com.bitwiserain.pbbg.app.db.repository.DexTableImpl
import com.bitwiserain.pbbg.app.db.repository.FriendsTableImpl
import com.bitwiserain.pbbg.app.db.repository.InventoryTableImpl
import com.bitwiserain.pbbg.app.db.repository.ItemHistoryTableImpl
import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTableImpl
import com.bitwiserain.pbbg.app.db.repository.SquadTableImpl
import com.bitwiserain.pbbg.app.db.repository.UnitTableImpl
import com.bitwiserain.pbbg.app.db.repository.UserStatsTableImpl
import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.db.repository.battle.BattleEnemyTableImpl
import com.bitwiserain.pbbg.app.db.repository.battle.BattleSessionTableImpl
import com.bitwiserain.pbbg.app.db.repository.farm.MaterializedPlantTableImpl
import com.bitwiserain.pbbg.app.db.repository.farm.PlotListTableImpl
import com.bitwiserain.pbbg.app.db.repository.farm.PlotTableImpl
import com.bitwiserain.pbbg.app.db.repository.market.MarketInventoryTableImpl
import com.bitwiserain.pbbg.app.db.repository.market.MarketTableImpl
import com.bitwiserain.pbbg.app.db.repository.mine.MineCellTableImpl
import com.bitwiserain.pbbg.app.db.repository.mine.MineSessionTableImpl
import com.bitwiserain.pbbg.app.db.usecase.AboutUCImpl
import com.bitwiserain.pbbg.app.db.usecase.BattleUCImpl
import com.bitwiserain.pbbg.app.db.usecase.DexUCImpl
import com.bitwiserain.pbbg.app.db.usecase.EquipmentUCImpl
import com.bitwiserain.pbbg.app.db.usecase.FarmUCImpl
import com.bitwiserain.pbbg.app.db.usecase.FriendsUCImpl
import com.bitwiserain.pbbg.app.db.usecase.GenerateBattleUCImpl
import com.bitwiserain.pbbg.app.db.usecase.GetBattleUCImpl
import com.bitwiserain.pbbg.app.db.usecase.InventoryUCImpl
import com.bitwiserain.pbbg.app.db.usecase.ItemUCImpl
import com.bitwiserain.pbbg.app.db.usecase.MarketUCImpl
import com.bitwiserain.pbbg.app.db.usecase.UnitUCImpl
import com.bitwiserain.pbbg.app.db.usecase.UserProfileUCImpl
import com.bitwiserain.pbbg.app.domain.usecase.ChangePasswordUCImpl
import com.bitwiserain.pbbg.app.domain.usecase.GetUserStatsUCImpl
import com.bitwiserain.pbbg.app.domain.usecase.LoginUCImpl
import com.bitwiserain.pbbg.app.domain.usecase.RegisterUserUCImpl
import com.bitwiserain.pbbg.app.domain.usecase.mine.ExitMineImpl
import com.bitwiserain.pbbg.app.domain.usecase.mine.GenerateMineImpl
import com.bitwiserain.pbbg.app.domain.usecase.mine.GetAvailableMinesImpl
import com.bitwiserain.pbbg.app.domain.usecase.mine.GetMineImpl
import com.bitwiserain.pbbg.app.domain.usecase.mine.SubmitMineActionImpl
import com.bitwiserain.pbbg.app.route.api.about
import com.bitwiserain.pbbg.app.route.api.battleAPI
import com.bitwiserain.pbbg.app.route.api.dexAPI
import com.bitwiserain.pbbg.app.route.api.farm
import com.bitwiserain.pbbg.app.route.api.friends
import com.bitwiserain.pbbg.app.route.api.inventoryAPI
import com.bitwiserain.pbbg.app.route.api.item
import com.bitwiserain.pbbg.app.route.api.loginAPI
import com.bitwiserain.pbbg.app.route.api.market
import com.bitwiserain.pbbg.app.route.api.mine
import com.bitwiserain.pbbg.app.route.api.registerAPI
import com.bitwiserain.pbbg.app.route.api.settings
import com.bitwiserain.pbbg.app.route.api.squadAPI
import com.bitwiserain.pbbg.app.route.api.unit
import com.bitwiserain.pbbg.app.route.api.user
import com.bitwiserain.pbbg.app.route.api.userStats
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.ApplicationConfigurationException
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock

enum class ApplicationEnvironment {
    DEV,
    PROD
}
lateinit var APP_ENVIRONMENT: ApplicationEnvironment

fun Application.main() {
    mainWithDependencies(Clock.systemUTC())
}

fun Application.mainWithDependencies(clock: Clock) {
    APP_ENVIRONMENT = when (environment.config.propertyOrNull("ktor.environment")?.getString()) {
        "dev" -> ApplicationEnvironment.DEV
        "prod" -> ApplicationEnvironment.PROD
        else -> throw ApplicationConfigurationException("Environment (KTOR_ENV) must be either dev or prod.")
    }

    /*************
     * Set up db *
     *************/
    val jdbcAddress = environment.config.property("jdbc.address").getString()

    val db = Database.connect("jdbc:$jdbcAddress", when {
        jdbcAddress.startsWith("h2:") -> org.h2.Driver::class.qualifiedName!!
        jdbcAddress.startsWith("postgresql:") -> org.postgresql.Driver::class.qualifiedName!!
        else -> throw RuntimeException("Only H2 and PostgreSQL databases are currently supported.")
    })

    val transaction: Transaction = object : Transaction {
        override fun <T> invoke(block: () -> T): T = transaction(db) { block() }
    }

    SchemaHelper.createTables(transaction)

    install(CallLogging)

    // Tables
    val battleEnemyTable = BattleEnemyTableImpl()
    val battleSessionTable = BattleSessionTableImpl()
    val dexTable = DexTableImpl()
    val friendsTable = FriendsTableImpl()
    val inventoryTable = InventoryTableImpl()
    val itemHistoryTable = ItemHistoryTableImpl()
    val marketTable = MarketTableImpl()
    val marketInventoryTable = MarketInventoryTableImpl()
    val materializedItemTable = MaterializedItemTableImpl()
    val materializedPlantTable = MaterializedPlantTableImpl()
    val mineCellTable = MineCellTableImpl()
    val mineSessionTable = MineSessionTableImpl()
    val plotTable = PlotTableImpl()
    val plotListTable = PlotListTableImpl()
    val squadTable = SquadTableImpl()
    val unitTable = UnitTableImpl()
    val userTable = UserTableImpl()
    val userStatsTable = UserStatsTableImpl()

    val getUserStats = GetUserStatsUCImpl(transaction, userStatsTable)
    val changePassword = ChangePasswordUCImpl(transaction, userTable)
    val registerUser = RegisterUserUCImpl(
        transaction, clock, dexTable, inventoryTable, itemHistoryTable, marketTable, marketInventoryTable, materializedItemTable, plotTable, plotListTable, squadTable, unitTable,
        userTable, userStatsTable
    )
    val generateMine = GenerateMineImpl(transaction, mineCellTable, mineSessionTable, userStatsTable)
    val getAvailableMines = GetAvailableMinesImpl(transaction, userStatsTable)
    val getMine = GetMineImpl(transaction, mineCellTable, mineSessionTable)
    val exitMine = ExitMineImpl(transaction, mineSessionTable)
    val submitMineAction = SubmitMineActionImpl(transaction, clock, dexTable, inventoryTable, itemHistoryTable, materializedItemTable, mineCellTable, mineSessionTable, userStatsTable)
    val login = LoginUCImpl(transaction, userTable)
    val marketUC = MarketUCImpl(transaction, dexTable, inventoryTable, marketInventoryTable, materializedItemTable, userStatsTable)
    val inventoryUC = InventoryUCImpl(transaction, inventoryTable)
    val itemUC = ItemUCImpl(transaction, itemHistoryTable, materializedItemTable, userTable)
    val farmUC = FarmUCImpl(transaction, clock, dexTable, inventoryTable, itemHistoryTable, materializedItemTable, materializedPlantTable, plotTable, plotListTable, userStatsTable)
    val equipmentUC = EquipmentUCImpl(transaction, inventoryTable)
    val unitUC = UnitUCImpl(transaction, battleSessionTable, squadTable, unitTable)
    val battleUC = BattleUCImpl(transaction, battleEnemyTable, battleSessionTable, squadTable, unitTable)
    val generateBattle = GenerateBattleUCImpl(transaction, battleEnemyTable, battleSessionTable, squadTable, unitTable)
    val getBattle = GetBattleUCImpl(transaction, battleEnemyTable, battleSessionTable, squadTable)
    val dexUC = DexUCImpl(transaction, dexTable)
    val userProfileUC = UserProfileUCImpl(transaction, friendsTable, userTable)
    val friendsUC = FriendsUCImpl(transaction, friendsTable, userTable)
    val aboutUC = AboutUCImpl()

    install(ContentNegotiation) {
        // Handles "application/json" content type
        json(json = Json {
            explicitNulls = true
            encodeDefaults = true
        })
    }

    val jwtRealm = environment.config.property("jwt.realm").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    install(Authentication) {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate {
                it.payload.getClaim("user.id").asInt()?.let {
                    transaction {
                        userTable.getUserById(it)
                    }
                }
            }
        }
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowNonSimpleContentTypes = true
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondError()
            cause.printStackTrace()
        }
    }
    routing {
        route("/api") {
            registerAPI(registerUser)
            loginAPI(login)
            item(itemUC)
            unit(unitUC)
            authenticate(optional = false) {
                userStats(getUserStats)
                inventoryAPI(inventoryUC, equipmentUC)
                market(marketUC)
                battleAPI(battleUC, generateBattle, getBattle)
                mine(submitMineAction, getMine, getAvailableMines, generateMine, exitMine)
                farm(farmUC, clock)
                dexAPI(dexUC)
                squadAPI(unitUC)
                friends(friendsUC)
                settings(changePassword)
                about(aboutUC)
            }
            authenticate(optional = true) {
                user(userProfileUC)
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

/***************************
 * Ktor-related extensions *
 ***************************/

suspend inline fun <reified T : Any> ApplicationCall.respondSuccess(data: T? = null, status: HttpStatusCode = HttpStatusCode.OK) {
    respond(
        status,
        buildJsonObject {
            put("status", "success")
            put("data", Json.encodeToJsonElement(data))
        }
    )
}

suspend inline fun ApplicationCall.respondSuccess(status: HttpStatusCode = HttpStatusCode.OK) {
    respond(
        status,
        buildJsonObject {
            put("status", "success")
            put("data", JsonNull)
        }
    )
}

suspend inline fun <reified T : Any> ApplicationCall.respondFail(data: T? = null, status: HttpStatusCode = HttpStatusCode.BadRequest) {
    respond(
        status,
        buildJsonObject {
            put("status", "fail")
            put("data", Json.encodeToJsonElement(data))
        }
    )
}

suspend inline fun ApplicationCall.respondFail(status: HttpStatusCode = HttpStatusCode.BadRequest) {
    respond(
        status,
        buildJsonObject {
            put("status", "fail")
            put("data", JsonNull)
        }
    )
}

suspend inline fun ApplicationCall.respondError(message: String = "", status: HttpStatusCode = HttpStatusCode.InternalServerError) {
    respond(
        status,
        buildJsonObject {
            put("status", "error")
            put("message", message)
        }
    )
}

val ApplicationCall.user
    get() = authentication.principal<User>()!!

val ApplicationCall.userOptional
    get() = authentication.principal<User>()

fun Application.makeToken(userId: Int): String = JWT.create()
    .withIssuer(environment.config.property("jwt.issuer").getString())
    .withClaim("user.id", userId)
    .sign(Algorithm.HMAC256(environment.config.property("jwt.secret").getString()))

val ApplicationRequest.serverRootURL get() = with(local) {
    "$scheme://$serverHost${if (serverPort != 80) ":$serverPort" else ""}"
}

/**********
 * BCrypt *
 **********/
object BCryptHelper {
    fun hashPassword(password: String): ByteArray = BCrypt.withDefaults().hash(12, password.toByteArray())
    fun verifyPassword(attemptedPassword: String, expectedPasswordHash: ByteArray) = BCrypt.verifyer().verify(attemptedPassword.toByteArray(), expectedPasswordHash).verified
}

/**********
 * Schema *
 **********/
object SchemaHelper {
    fun createTables(transaction: Transaction) = transaction {
        SchemaUtils.create(
            UserTableImpl.Exposed, MineSessionTableImpl.Exposed, MineCellTableImpl.Exposed, MaterializedItemTableImpl.Exposed, InventoryTableImpl.Exposed,
            UserStatsTableImpl.Exposed, UnitTableImpl.Exposed, SquadTableImpl.Exposed, BattleSessionTableImpl.Exposed, BattleEnemyTableImpl.Exposed, DexTableImpl.Exposed,
            MarketTableImpl.Exposed, MarketInventoryTableImpl.Exposed, ItemHistoryTableImpl.Exposed, PlotTableImpl.Exposed, PlotListTableImpl.Exposed,
            MaterializedPlantTableImpl.Exposed, FriendsTableImpl.Exposed
        )
    }

    fun dropTables(transaction: Transaction) = transaction {
        SchemaUtils.drop(
            UserTableImpl.Exposed, MineSessionTableImpl.Exposed, MineCellTableImpl.Exposed, MaterializedItemTableImpl.Exposed, InventoryTableImpl.Exposed,
            UserStatsTableImpl.Exposed, UnitTableImpl.Exposed, SquadTableImpl.Exposed, BattleSessionTableImpl.Exposed, BattleEnemyTableImpl.Exposed, DexTableImpl.Exposed,
            MarketTableImpl.Exposed, MarketInventoryTableImpl.Exposed, ItemHistoryTableImpl.Exposed, PlotTableImpl.Exposed, PlotListTableImpl.Exposed,
            MaterializedPlantTableImpl.Exposed, FriendsTableImpl.Exposed
        )
    }
}

/***************
 * Patch Notes *
 ***************/
object PatchNotesHelper {
    fun getAll(): List<String> {
        return APP_VERSIONS.reversed().map { javaClass.getResource("/patch-notes/${it}.md").readText() }
    }
}
