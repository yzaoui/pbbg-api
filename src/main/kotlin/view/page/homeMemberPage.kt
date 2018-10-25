package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.template.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.*

fun homeMemberPage(memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Home", memberPageVM).apply {
    endOfBody {
        script(src = "/js/webcomponents-bundle-2.0.0.js") {}
        script(src = "/js/component/pbbg-progress-bar.js") {}
        script(src = "/js/home.js") {}
    }
}
