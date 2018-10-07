package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.model.UserStatsVM
import com.bitwiserain.pbbg.view.template.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.p
import kotlinx.html.script

fun homeMemberPage(userStatsVM: UserStatsVM, memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Home", memberPageVM).apply {
    content {
        p {
            +"Mining Level ${userStatsVM.miningLevelProgress.level}: ${userStatsVM.miningLevelProgress.expThisLevel} / ${userStatsVM.miningLevelProgress.totalExpToNextLevel} exp"
        }
    }
    endOfBody {
        script(src = "/js/home.js") {}
    }
}
