package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.model.UserStatsVM
import com.bitwiserain.pbbg.view.template.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.*

fun homeMemberPage(userStatsVM: UserStatsVM, memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Home", memberPageVM).apply {
    content {
        div {
            span {
                +"Mining Level ${userStatsVM.miningLevelProgress.level}: "
            }
            meter {
                value = userStatsVM.miningLevelProgress.expThisLevel.toString()
                min = 0.toString()
                max = userStatsVM.miningLevelProgress.totalExpToNextLevel.toString()
            }
            span {
                +" ${userStatsVM.miningLevelProgress.expThisLevel} / ${userStatsVM.miningLevelProgress.totalExpToNextLevel} Exp."
            }
        }
    }
    endOfBody {
        script(src = "/js/home.js") {}
    }
}
