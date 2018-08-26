package miner.view

import io.ktor.html.Template
import kotlinx.html.*
import miner.PASSWORD_REGEX
import miner.PASSWORD_REGEX_DESCRIPTION
import miner.USERNAME_REGEX
import miner.USERNAME_REGEX_DESCRIPTION
import miner.view.template.GuestTemplate

fun registerPage(registerURL: String, loginURL: String, guestPageVM: GuestPageVM, errors: List<String>? = null): Template<HTML> = GuestTemplate("Register", guestPageVM).apply {
    content {
        if (errors != null) {
            ul {
                errors.forEach {
                    li { +it }
                }
            }
        }
        form(action = registerURL, method = FormMethod.post) {
            input(type = InputType.text, name = "username") {
                required = true
                placeholder = "Username"
                autoFocus = true
                pattern = USERNAME_REGEX
                title = USERNAME_REGEX_DESCRIPTION
            }
            input(type = InputType.password, name = "password") {
                required = true
                placeholder = "Password"
                pattern = PASSWORD_REGEX
                title = PASSWORD_REGEX_DESCRIPTION
            }
            button(type = ButtonType.submit) { +"Register" }
        }
        a(href = loginURL) { +"Existing user? Log in" }
    }
}
