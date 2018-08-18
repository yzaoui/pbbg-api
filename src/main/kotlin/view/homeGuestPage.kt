package miner.view

import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.br

fun homeGuestPage(mineURL: String, registerURL: String, loginURL: String): Template<HTML> = MainTemplate("Home").apply {
    content {
        a(href = mineURL) { +"Click here to mine!" }
        br { }
        a(href = registerURL) { +"Register" }
        br { }
        a(href = loginURL) { +"Log in" }
    }
}
