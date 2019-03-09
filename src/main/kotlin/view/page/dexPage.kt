package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.template.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.script

fun dexPage(memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Dex", memberPageVM).apply {
    endOfBody {
        script(src = "/js/dex.js") {}
    }
}
