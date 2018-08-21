package miner.view

import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.script

fun inventoryPage(homeURL: String): Template<HTML> = MainTemplate("Inventory").apply {
    content {
        a(href = homeURL) { +"Return home" }
    }
    endOfBody {
        script(src = "/js/inventory.js") {}
    }
}
