package miner.view

import io.ktor.html.Template
import kotlinx.html.*

fun registerPage(registerURL: String): Template<HTML> = MainTemplate("Register").apply {
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
    }
}
