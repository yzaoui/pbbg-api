package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.template.GuestPageVM
import com.bitwiserain.pbbg.view.template.GuestTemplate
import io.ktor.html.Template
import kotlinx.html.*

fun homeGuestPage(registerURL: String, loginURL: String, guestPageVM: GuestPageVM): Template<HTML> = GuestTemplate("Home", guestPageVM).apply {
    content {
        img(alt = "Banner", src = "/img/banner.png") {
            style = "align-self: center;"
        }
        a(href = registerURL, classes = "btn") {
            style = "align-self: center;"
            +"Register"
        }
        br()
        a(href = loginURL, classes = "btn") {
            style = "align-self: center;"
            +"Log in"
        }
    }
}
