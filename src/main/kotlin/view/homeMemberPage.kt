package pbbg.view

import io.ktor.html.Template
import kotlinx.html.*
import pbbg.view.template.MemberTemplate

fun homeMemberPage(mineURL: String, inventoryURL: String, equipmentURL: String, memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Home", memberPageVM).apply {
    content {
        a(href = mineURL) { +"> Mine" }
        br { }
        a(href = inventoryURL) { +"> Inventory" }
        br { }
        a(href = equipmentURL) { +"> Equipment" }
    }
}
