package com.bitwiserain.pbbg.view

import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.*

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
