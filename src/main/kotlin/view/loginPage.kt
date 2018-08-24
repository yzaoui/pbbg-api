package miner.view

import io.ktor.html.Template
import kotlinx.html.*
import miner.view.template.GuestTemplate
import miner.view.template.MemberTemplate

fun loginPage(loginURL: String, registerURL: String, guestPageVM: GuestPageVM): Template<HTML> = GuestTemplate("Log in", guestPageVM).apply {
    content {
        form(action = loginURL, method = FormMethod.post) {
            input(type = InputType.text, name = "username") {
                required = true
                placeholder = "Username"
                autoFocus = true
            }
            input(type = InputType.password, name = "password") {
                required = true
                placeholder = "Password"
            }
            button(type = ButtonType.submit) { +"Log in" }
        }
        a(href = registerURL) { +"New user? Register" }
    }
}
