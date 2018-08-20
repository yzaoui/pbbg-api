package miner.view

import io.ktor.html.Template
import kotlinx.html.*

fun equipmentPage(homeURL: String): Template<HTML> = MainTemplate("Equipment").apply {
    content {
        a(href = homeURL) { +"Return home" }
        br { }
        div {
            +"Equipped pickaxe: "
            span {
                id = "equipped-pickaxe"
            }
        }
    }
    endOfBody {
        script(src = "/js/equipment.js") {}
    }
}
