package com.bitwiserain.pbbg.view.template

import io.ktor.html.Placeholder
import io.ktor.html.Template
import io.ktor.html.insert
import kotlinx.html.*

class MemberTemplate(private val pageTitle: String, private val memberPageVM: MemberPageVM) : Template<HTML> {
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
                nav(classes = "sidebar") {
                    div(classes = "username") {
                        +memberPageVM.user.username
                    }
                    a(href = memberPageVM.homeUrl, classes = "sidebar-item") {
                        span(classes = "sidebar-item-icon") { +"""🏠""" }
                        span { +"Home" }
                    }
                    a(href = memberPageVM.squadUrl, classes = "sidebar-item") {
                        span(classes = "sidebar-item-icon") { +"""👥""" }
                        span { +"Squad" }
                    }
                    a(href = memberPageVM.inventoryUrl, classes = "sidebar-item") {
                        span(classes = "sidebar-item-icon") { +"""🎒""" }
                        span { +"Inventory" }
                    }
                    a(href = memberPageVM.battleUrl, classes = "sidebar-item") {
                        span(classes = "sidebar-item-icon") { +"""⚔️""" } // Unicode variation selector 16
                        span { +"Battle" }
                    }
                    a(href = memberPageVM.mineUrl, classes = "sidebar-item") {
                        span(classes = "sidebar-item-icon") { +"""⛏️""" } // Unicode variation selector 16
                        span { +"Mine" }
                    }
                    a(href = memberPageVM.dexUrl, classes = "sidebar-item") {
                        span(classes = "sidebar-item-icon") { +"""📚""" }
                        span { +"Dex" }
                    }
                    a(href = memberPageVM.settingsUrl, classes = "sidebar-item") {
                        span(classes = "sidebar-item-icon") { +"""⚙️""" } // Unicode variation selector 16
                        span { +"Settings" }
                    }
                    div(classes = "sidebar-logout") {
                        form(action = memberPageVM.logoutUrl, method = FormMethod.post) {
                            button(type = ButtonType.submit) {
                                +"Log out"
                            }
                        }
                    }
                }
                main {
                    id = "main"
                    insert(content)
                }
            }
            footer {
                +"BitwiseRain © 2019"
            }
            script(src = "/js/helpers.js") {}
            insert(endOfBody)
        }
    }
}
