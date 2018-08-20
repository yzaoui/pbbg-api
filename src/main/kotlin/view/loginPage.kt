package miner.view

import io.ktor.html.Template
import kotlinx.html.*

fun loginPage(loginURL: String, homeURL: String, registerURL: String): Template<HTML> = MainTemplate("Log in").apply {
    content {
        a(href = homeURL) { +"Return home" }
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
