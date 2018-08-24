package miner.view

import io.ktor.html.Template
import kotlinx.html.*
import miner.view.template.MemberTemplate

fun homeMemberPage(mineURL: String, inventoryURL: String, equipmentURL: String, logoutURL: String, memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Home", memberPageVM).apply {
    content {
        a(href = mineURL) { +"> Mine" }
        br { }
        a(href = inventoryURL) { +"> Inventory" }
        br { }
        a(href = equipmentURL) { +"> Equipment" }
        br { }
        form(action = logoutURL, method = FormMethod.post) {
            button(type = ButtonType.submit) { +"Log out" }
        }
    }
}
