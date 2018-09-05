package pbbg.view.template

import io.ktor.html.Placeholder
import io.ktor.html.Template
import io.ktor.html.insert
import kotlinx.html.*
import pbbg.view.MemberPageVM

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
                        div {
                            a(href = memberPageVM.home.url) {
                                +memberPageVM.home.label
                            }
                        }
                        div {
                            form(action = memberPageVM.logout.url, method = FormMethod.post) {
                                button(type = ButtonType.submit) { +memberPageVM.logout.label }
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
