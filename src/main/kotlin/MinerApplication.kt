package miner

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.install
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.freemarker.FreeMarker
import io.ktor.jackson.jackson
import io.ktor.locations.Locations
import io.ktor.locations.locations
import io.ktor.pipeline.PipelineContext
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import miner.data.MineContentsTable
import miner.data.MineSessionTable
import miner.data.UserTable
import miner.domain.usecase.MiningUCImpl
import miner.domain.usecase.UserUCImpl
import miner.route.api.pickaxe
import org.h2.Driver
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import route.web.*

data class MinerSession(val userId: Int)

fun Application.main() {
    Database.connect("jdbc:h2:./testDB", Driver::class.qualifiedName!!)
    transaction {
        addLogger(Slf4jSqlDebugLogger)
        SchemaUtils.create(UserTable, MineSessionTable, MineContentsTable)
    }

    val userUC = UserUCImpl()
    val miningUC = MiningUCImpl()

    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "")
    }
    install(Sessions) {
        cookie<MinerSession>("miner_session") {
            cookie.path = "/"
        }
    }
    install(ContentNegotiation) {
        jackson {  }
    }
    routing {
        index(userUC)
        login(userUC)
        logout()
        register(userUC)
        mineWeb(userUC, miningUC)
        route("/api") {
            pickaxe()
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

fun Route.href(location: Any) = application.locations.href(location)
fun PipelineContext<Unit, ApplicationCall>.href(location: Any) = application.locations.href(location)
