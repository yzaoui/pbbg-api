package com.bitwiserain.pbbg.view

import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.script

fun inventoryPage(memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Inventory", memberPageVM).apply {
    endOfBody {
        script(src = "/js/inventory.js") {}
    }
}
