package com.bitwiserain.pbbg

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.db.repository.DexTableImpl
import com.bitwiserain.pbbg.db.repository.FriendsTableImpl
import com.bitwiserain.pbbg.db.repository.InventoryTableImpl
import com.bitwiserain.pbbg.db.repository.ItemHistoryTableImpl
import com.bitwiserain.pbbg.db.repository.MaterializedItemTableImpl
import com.bitwiserain.pbbg.db.repository.SquadTableImpl
import com.bitwiserain.pbbg.db.repository.UnitTableImpl
import com.bitwiserain.pbbg.db.repository.UserStatsTableImpl
import com.bitwiserain.pbbg.db.repository.UserTableImpl
import com.bitwiserain.pbbg.db.repository.battle.BattleEnemyTableImpl
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTableImpl
import com.bitwiserain.pbbg.db.repository.farm.MaterializedPlantTableImpl
import com.bitwiserain.pbbg.db.repository.farm.PlotTableImpl
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTableImpl
import com.bitwiserain.pbbg.db.repository.market.MarketTableImpl
import com.bitwiserain.pbbg.db.repository.mine.MineCellTableImpl
import com.bitwiserain.pbbg.db.repository.mine.MineSessionTableImpl
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
    val squadTable = SquadTableImpl()
    val unitTable = UnitTableImpl()
    val userTable = UserTableImpl()
    val userStatsTable = UserStatsTableImpl()

    val getUserStats = GetUserStatsUCImpl(db, userStatsTable)
    val changePassword = ChangePasswordUCImpl(db, userTable)
    val registerUser = RegisterUserUCImpl(
        db, clock, dexTable, inventoryTable, itemHistoryTable, marketTable, marketInventoryTable, materializedItemTable, plotTable, squadTable, unitTable, userTable, userStatsTable
    )
    val login = LoginUCImpl(db, userTable)
    val marketUC = MarketUCImpl(db, dexTable, inventoryTable, marketInventoryTable, materializedItemTable, userStatsTable)
    val inventoryUC = InventoryUCImpl(db)
    val itemUC = ItemUCImpl(db, itemHistoryTable, materializedItemTable, userTable)
    val miningUC = MiningUCImpl(db, clock, dexTable, inventoryTable, itemHistoryTable, materializedItemTable, mineCellTable, mineSessionTable, userStatsTable)
    val farmUC = FarmUCImpl(db, clock, dexTable, inventoryTable, itemHistoryTable, materializedItemTable, materializedPlantTable, plotTable, userStatsTable)
    val equipmentUC = EquipmentUCImpl(db)
    val unitUC = UnitUCImpl(db, battleSessionTable, squadTable, unitTable)
    val battleUC = BattleUCImpl(db, battleEnemyTable, battleSessionTable, squadTable, unitTable)
    val dexUC = DexUCImpl(db, dexTable)
    val userProfileUC = UserProfileUCImpl(db, friendsTable, userTable)
    val friendsUC = FriendsUCImpl(db, friendsTable, userTable)
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
                        userTable.getUserById(it)
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
            UserTableImpl.Exposed, MineSessionTableImpl.Exposed, MineCellTableImpl.Exposed, MaterializedItemTableImpl.Exposed, InventoryTableImpl.Exposed,
            UserStatsTableImpl.Exposed, UnitTableImpl.Exposed, SquadTableImpl.Exposed, BattleSessionTableImpl.Exposed, BattleEnemyTableImpl.Exposed, DexTableImpl.Exposed,
            MarketTableImpl.Exposed, MarketInventoryTableImpl.Exposed, ItemHistoryTableImpl.Exposed, PlotTableImpl.Exposed, MaterializedPlantTableImpl.Exposed,
            FriendsTableImpl.Exposed
        )
    }

    fun dropTables(db: Database) = transaction(db) {
        SchemaUtils.drop(
            UserTableImpl.Exposed, MineSessionTableImpl.Exposed, MineCellTableImpl.Exposed, MaterializedItemTableImpl.Exposed, InventoryTableImpl.Exposed,
            UserStatsTableImpl.Exposed, UnitTableImpl.Exposed, SquadTableImpl.Exposed, BattleSessionTableImpl.Exposed, BattleEnemyTableImpl.Exposed, DexTableImpl.Exposed,
            MarketTableImpl.Exposed, MarketInventoryTableImpl.Exposed, ItemHistoryTableImpl.Exposed, PlotTableImpl.Exposed, MaterializedPlantTableImpl.Exposed,
            FriendsTableImpl.Exposed
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
