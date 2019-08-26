package com.bitwiserain.pbbg.view.template

import com.bitwiserain.pbbg.view.SiteSection
import io.ktor.html.Placeholder
import io.ktor.html.Template
import io.ktor.html.insert
import kotlinx.html.*

class MemberTemplate(private val pageTitle: String, private val memberPageVM: MemberPageVM, private val currentSiteSection: SiteSection) : Template<HTML> {
    val headContent = Placeholder<HEAD>()
    val content = Placeholder<MAIN>()
    val endOfBody = Placeholder<BODY>()
    override fun HTML.apply() {
        head {
            title { +pageTitle }
            link(href = "/css/normalize.css", rel = "stylesheet")
            link(href = "/css/common.css", rel = "stylesheet")
            link(rel = "icon", href = "/img/favicon-16.png", type = "image/png") {
                sizes = "16x16"
            }
            link(rel = "icon", href = "/img/favicon-32.png", type = "image/png") {
                sizes = "32x32"
            }
            meta(name = "viewport", content = "width=device-width")
            insert(headContent)
        }
        body {
            div(classes = "container") {
                nav(classes = "sidebar") {
                    div(classes = "username") {
                        +memberPageVM.user.username
                    }
                    div(classes = "navigation") {
                        a(href = memberPageVM.homeUrl, classes = "sidebar-item") {
                            highlightIfCurrentSiteSection(currentSiteSection, SiteSection.HOME)
                            span(classes = "sidebar-item-icon") { +"""🏠""" }
                            span { +"Home" }
                        }
                        a(href = memberPageVM.battleUrl, classes = "sidebar-item") {
                            highlightIfCurrentSiteSection(currentSiteSection, SiteSection.BATTLE)
                            span(classes = "sidebar-item-icon") { +"""⚔️""" } // Unicode variation selector 16
                            span { +"Battle" }
                        }
                        a(href = memberPageVM.mineUrl, classes = "sidebar-item") {
                            highlightIfCurrentSiteSection(currentSiteSection, SiteSection.MINE)
                            span(classes = "sidebar-item-icon") { +"""⛏️""" } // Unicode variation selector 16
                            span { +"Mine" }
                        }
                        a(href = memberPageVM.settingsUrl, classes = "sidebar-item") {
                            highlightIfCurrentSiteSection(currentSiteSection, SiteSection.SETTINGS)
                            span(classes = "sidebar-item-icon") { +"""⚙️""" } // Unicode variation selector 16
                            span { +"Settings" }
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
                +"BitwiseRain © 2019"
            }
            script(src = "/js/helpers.js") {}
            insert(endOfBody)
        }
    }
}

private fun A.highlightIfCurrentSiteSection(currentSiteSection: SiteSection, thisSiteSection: SiteSection) {
    if (currentSiteSection == thisSiteSection) classes += "current-site-section"
}
