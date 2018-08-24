package miner.view

import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.script
import miner.view.template.MemberTemplate

fun inventoryPage(homeURL: String, memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Inventory", memberPageVM).apply {
    content {
        a(href = homeURL) { +"Return home" }
    }
    endOfBody {
        script(src = "/js/inventory.js") {}
    }
}
