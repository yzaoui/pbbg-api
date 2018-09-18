package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.PASSWORD_REGEX
import com.bitwiserain.pbbg.PASSWORD_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.USERNAME_REGEX
import com.bitwiserain.pbbg.USERNAME_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.view.GuestPageVM
import com.bitwiserain.pbbg.view.template.GuestTemplate
import io.ktor.html.Template
import kotlinx.html.*

fun registerPage(registerURL: String, loginURL: String, guestPageVM: GuestPageVM, errors: List<String>? = null): Template<HTML> = GuestTemplate("Register", guestPageVM).apply {
    content {
        if (errors != null) {
            ul(classes = "form-errors") {
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
