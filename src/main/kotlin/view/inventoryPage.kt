package pbbg.view

import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.script
import pbbg.view.template.MemberTemplate

fun inventoryPage(memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Inventory", memberPageVM).apply {
    endOfBody {
        script(src = "/js/inventory.js") {}
    }
}
