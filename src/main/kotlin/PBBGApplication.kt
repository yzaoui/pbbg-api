package com.bitwiserain.pbbg

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.db.repository.FriendsTable
import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.db.repository.farm.MaterializedPlantTable
import com.bitwiserain.pbbg.db.repository.farm.PlotTable
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.db.repository.market.MarketTable
import com.bitwiserain.pbbg.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.db.repository.mine.MineSessionTable
import com.bitwiserain.pbbg.db.usecase.AboutUCImpl
import com.bitwiserain.pbbg.db.usecase.BattleUCImpl
import com.bitwiserain.pbbg.db.usecase.DexUCImpl
import com.bitwiserain.pbbg.db.usecase.EquipmentUCImpl
import com.bitwiserain.pbbg.db.usecase.FarmUCImpl
import com.bitwiserain.pbbg.db.usecase.FriendsUCImpl
import com.bitwiserain.pbbg.db.usecase.InventoryUCImpl
import com.bitwiserain.pbbg.db.usecase.ItemUCImpl
import com.bitwiserain.pbbg.db.usecase.MarketUCImpl
import com.bitwiserain.pbbg.db.usecase.MiningUCImpl
import com.bitwiserain.pbbg.db.usecase.UnitUCImpl
import com.bitwiserain.pbbg.db.usecase.UserProfileUCImpl
import com.bitwiserain.pbbg.domain.usecase.ChangePasswordUCImpl
import com.bitwiserain.pbbg.domain.usecase.GetUserStatsUCImpl
import com.bitwiserain.pbbg.domain.usecase.LoginUCImpl
import com.bitwiserain.pbbg.domain.usecase.RegisterUserUCImpl
import com.bitwiserain.pbbg.route.api.about
import com.bitwiserain.pbbg.route.api.battleAPI
import com.bitwiserain.pbbg.route.api.dexAPI
import com.bitwiserain.pbbg.route.api.farm
import com.bitwiserain.pbbg.route.api.friends
import com.bitwiserain.pbbg.route.api.inventoryAPI
import com.bitwiserain.pbbg.route.api.item
import com.bitwiserain.pbbg.route.api.loginAPI
import com.bitwiserain.pbbg.route.api.market
import com.bitwiserain.pbbg.route.api.mine
import com.bitwiserain.pbbg.route.api.registerAPI
import com.bitwiserain.pbbg.route.api.settings
import com.bitwiserain.pbbg.route.api.squadAPI
import com.bitwiserain.pbbg.route.api.unit
import com.bitwiserain.pbbg.route.api.user
import com.bitwiserain.pbbg.route.api.userStats
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock

enum class ApplicationEnvironment {
    DEV,
    PROD
}
lateinit var appEnvironment: ApplicationEnvironment
lateinit var API_ROOT: String

fun Application.main() {
    mainWithDependencies(Clock.systemUTC())
}

fun Application.mainWithDependencies(clock: Clock) {
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
        jdbcAddress.startsWith("postgresql:") -> org.postgresql.Driver::class.qualifiedName!!
        else -> throw RuntimeException("Only H2 and PostgreSQL databases are currently supported.")
    })

    SchemaHelper.createTables(db)

    install(CallLogging)

    val getUserStats = GetUserStatsUCImpl(db)
    val changePassword = ChangePasswordUCImpl(db)
    val registerUser = RegisterUserUCImpl(db, clock)
    val login = LoginUCImpl(db)
    val marketUC = MarketUCImpl(db)
    val inventoryUC = InventoryUCImpl(db)
    val itemUC = ItemUCImpl(db)
    val miningUC = MiningUCImpl(db, clock)
    val farmUC = FarmUCImpl(db, clock)
    val equipmentUC = EquipmentUCImpl(db)
    val unitUC = UnitUCImpl(db)
    val battleUC = BattleUCImpl(db)
    val dexUC = DexUCImpl(db)
    val userProfileUC = UserProfileUCImpl(db)
    val friendsUC = FriendsUCImpl(db)
    val aboutUC = AboutUCImpl()

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
                it.payload.getClaim("user.id").asInt()?.let {
                    transaction(db) {
                        UserTable.getUserById(it)
                    }
                }
            }
        }
    }
    install(CORS) {
        anyHost()
        header(HttpHeaders.Authorization)
        allowNonSimpleContentTypes = true
    }
    install(StatusPages) {
        exception<Throwable> {
            call.respondError()
            it.printStackTrace()
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
                battleAPI(battleUC)
                mine(miningUC)
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

val ApplicationCall.userOptional
    get() = authentication.principal<User>()

fun Application.makeToken(userId: Int): String = JWT.create()
    .withIssuer(environment.config.property("jwt.issuer").getString())
    .withClaim("user.id", userId)
    .sign(Algorithm.HMAC256(environment.config.property("jwt.secret").getString()))

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
    fun createTables(db: Database) = transaction(db) {
        SchemaUtils.create(
            UserTable, MineSessionTable, MineCellTable, MaterializedItemTable, InventoryTable, UserStatsTable, UnitTable,
            SquadTable, BattleSessionTable, BattleEnemyTable, DexTable, MarketTable, MarketInventoryTable, ItemHistoryTable,
            PlotTable, MaterializedPlantTable, FriendsTable
        )
    }

    fun dropTables(db: Database) = transaction(db) {
        SchemaUtils.drop(
            UserTable, MineSessionTable, MineCellTable, MaterializedItemTable, InventoryTable, UserStatsTable, UnitTable,
            SquadTable, BattleSessionTable, BattleEnemyTable, DexTable, MarketTable, MarketInventoryTable, ItemHistoryTable,
            PlotTable, MaterializedPlantTable, FriendsTable
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
