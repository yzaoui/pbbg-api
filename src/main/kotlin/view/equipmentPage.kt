package miner.view

import io.ktor.html.Template
import kotlinx.html.*
import miner.view.template.MemberTemplate

fun equipmentPage(homeURL: String, memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Equipment", memberPageVM).apply {
    headContent {
        link(href = "/css/equipment.css", rel = "stylesheet")
    }
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
