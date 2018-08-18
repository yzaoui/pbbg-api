package route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import miner.MinerSession
import miner.domain.usecase.UserUseCase
import miner.href
import miner.view.homeGuestPage

@Location("/")
class IndexLocation

fun Route.index(userUC: UserUseCase) {
    get<IndexLocation> { _ ->
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }

        call.respondHtmlTemplate(
            homeGuestPage(
                mineURL = href(MineLocation()),
                registerURL = href(RegisterLocation()),
                loginURL = href(LoginLocation())
            )
        ) {}
    }
}
