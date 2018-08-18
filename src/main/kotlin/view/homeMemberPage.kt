package miner.view

import io.ktor.html.Template
import kotlinx.html.*

fun homeMemberPage(mineURL: String, logoutURL: String): Template<HTML> = MainTemplate("Home").apply {
    content {
        a(href = mineURL) { +"Click here to mine!" }
        br { }
        form(action = logoutURL, method = FormMethod.post) {
            button(type = ButtonType.submit) { +"Log out" }
        }
    }
}
