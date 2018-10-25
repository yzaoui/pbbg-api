package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.template.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.p

fun battlePage(memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Battle", memberPageVM).apply {
    content {
        p { +"Work in Progress." }
    }
}
