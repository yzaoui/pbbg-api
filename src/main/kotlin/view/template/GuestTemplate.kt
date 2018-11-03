package com.bitwiserain.pbbg.view.template

import io.ktor.html.Placeholder
import io.ktor.html.Template
import io.ktor.html.insert
import kotlinx.html.*

class GuestTemplate(private val pageTitle: String, private val guestPageVM: GuestPageVM) : Template<HTML> {
    val headContent = Placeholder<HEAD>()
    val content = Placeholder<MAIN>()
    val endOfBody = Placeholder<BODY>()
    override fun HTML.apply() {
        head {
            title { +pageTitle }
            link(href = "/css/common.css", rel = "stylesheet")
            link(rel = "icon", href = "/img/favicon-16.png", type = "image/png") {
                sizes = "16x16"
            }
            link(rel = "icon", href = "/img/favicon-32.png", type = "image/png") {
                sizes = "32x32"
            }
            insert(headContent)
        }
        body {
            div(classes = "container") {
                nav(classes = "sidebar sidebar-guest") {
                    a(href = "/") {
                        +"Index"
                    }
                    form(action = guestPageVM.loginURL, method = FormMethod.post, classes = "sidebar-login-form") {
                        input(type = InputType.text, name = "username") {
                            required = true
                            placeholder = "Username"
                        }
                        input(type = InputType.password, name = "password") {
                            required = true
                            placeholder = "Password"
                        }
                        button(type = ButtonType.submit) { +"Log in" }
                    }
                }
                main {
                    id = "main"
                    insert(content)
                }
            }
            footer {
                +"BitwiseRain © 2018"
            }
            insert(endOfBody)
        }
    }
}
