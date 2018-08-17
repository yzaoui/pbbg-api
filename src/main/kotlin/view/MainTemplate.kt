package miner.view

import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.head

class MainTemplate : Template<HTML> {
    override fun HTML.apply() {
        head {

        }
        body {
            +"Hello world!"
        }
    }
}
