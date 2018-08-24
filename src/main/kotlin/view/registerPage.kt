package miner.view

import io.ktor.html.Template
import kotlinx.html.*
import miner.view.template.GuestTemplate

fun registerPage(registerURL: String, loginURL: String, guestPageVM: GuestPageVM): Template<HTML> = GuestTemplate("Register", guestPageVM).apply {
    content {
        form(action = registerURL, method = FormMethod.post) {
            input(type = InputType.text, name = "username") {
                required = true
                placeholder = "Username"
                autoFocus = true
            }
            input(type = InputType.password, name = "password") {
                required = true
                placeholder = "Password"
            }
            button(type = ButtonType.submit) { +"Register" }
        }
        a(href = loginURL) { +"Existing user? Log in" }
    }
}
