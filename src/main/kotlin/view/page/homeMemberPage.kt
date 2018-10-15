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
                value = userStatsVM.miningLevelProgress.relativeExp.toString()
                min = 0.toString()
                max = userStatsVM.miningLevelProgress.relativeExpNextLevel.toString()
            }
            span {
                +" ${userStatsVM.miningLevelProgress.relativeExp} / ${userStatsVM.miningLevelProgress.relativeExpNextLevel} Exp."
            }
        }
    }
}
