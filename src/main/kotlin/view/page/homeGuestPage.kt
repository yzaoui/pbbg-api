package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.template.GuestPageVM
import com.bitwiserain.pbbg.view.template.GuestTemplate
import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.br

fun homeGuestPage(registerURL: String, loginURL: String, guestPageVM: GuestPageVM): Template<HTML> = GuestTemplate("Home", guestPageVM).apply {
    content {
        a(href = registerURL) { +"Register" }
        br { }
        a(href = loginURL) { +"Log in" }
    }
}
