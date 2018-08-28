package pbbg.view

import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.br
import pbbg.view.template.GuestTemplate

fun homeGuestPage(registerURL: String, loginURL: String, guestPageVM: GuestPageVM): Template<HTML> = GuestTemplate("Home", guestPageVM).apply {
    content {
        a(href = registerURL) { +"Register" }
        br { }
        a(href = loginURL) { +"Log in" }
    }
}
