package miner.view

import io.ktor.html.Template
import kotlinx.html.*

fun equipmentPage(homeURL: String): Template<HTML> = MainTemplate("Equipment").apply {
    content {
        a(href = homeURL) { +"Return home" }
        br { }
        +"Equipped pickaxe: "
        span {
            id = "equipped-pickaxe"
        }
        script(src = "/js/equipment.js") {}
    }
}
