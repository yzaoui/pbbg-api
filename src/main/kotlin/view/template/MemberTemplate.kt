package com.bitwiserain.pbbg.view.template

import io.ktor.html.Placeholder
import io.ktor.html.Template
import io.ktor.html.insert
import kotlinx.html.*

open class MemberTemplate(private val pageTitle: String, private val memberPageVM: MemberPageVM) : Template<HTML> {
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
                div(classes = "content") {
                    nav(classes = "sidebar") {
                        div(classes = "username") {
                            +memberPageVM.user.username
                        }
                        div(classes = "sidebar-home") {
                            a(href = memberPageVM.homeUrl) {
                                +"Home"
                            }
                        }
                        div(classes = "sidebar-inventory") {
                            a(href = memberPageVM.inventoryUrl) {
                                +"Inventory"
                            }
                        }
                        div(classes = "sidebar-mine") {
                            a(href = memberPageVM.mineUrl) {
                                +"Mine"
                            }
                        }
                        div(classes = "sidebar-settings") {
                            a(href = memberPageVM.settingsUrl) {
                                +"Settings"
                            }
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
                    +"BitwiseRain © 2018"
                }
            }
            insert(endOfBody)
        }
    }
}
