package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.a

fun homeMemberPage(equipmentURL: String, memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Home", memberPageVM).apply {
    content {
        a(href = equipmentURL) { +"> Equipment" }
    }
}
