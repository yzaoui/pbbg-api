package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.MemberPageVM
import com.bitwiserain.pbbg.view.model.UserStatsVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.p

fun homeMemberPage(equipmentURL: String, userStatsVM: UserStatsVM, memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Home", memberPageVM).apply {
    content {
        p {
            +"Mining Level ${userStatsVM.miningLevel}: ${userStatsVM.miningExpSinceLastLevel} / ${userStatsVM.miningExpToNextLevel} exp"
        }
        a(href = equipmentURL) { +"> Equipment" }
    }
}
