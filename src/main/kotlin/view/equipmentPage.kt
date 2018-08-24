package miner.view

import io.ktor.html.Template
import kotlinx.html.*
import miner.view.template.MemberTemplate

fun equipmentPage(memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Equipment", memberPageVM).apply {
    headContent {
        link(href = "/css/equipment.css", rel = "stylesheet")
    }
    content {
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
