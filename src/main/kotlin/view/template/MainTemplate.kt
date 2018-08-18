package miner.view

import io.ktor.html.Placeholder
import io.ktor.html.Template
import io.ktor.html.insert
import kotlinx.html.*

open class MainTemplate(private val pageTitle: String) : Template<HTML> {
    val content = Placeholder<BODY>()
    override fun HTML.apply() {
        head {
            title { +pageTitle }
            link(href = "/css/style.css", rel = "stylesheet")
        }
        body {
            insert(content)
        }
    }
}
