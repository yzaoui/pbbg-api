package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.template.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.link
import kotlinx.html.script

fun minePage(memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Mine", memberPageVM).apply {
    headContent {
        link(href = "/css/mine.css", rel = "stylesheet")
    }
    endOfBody {
        script(src = "/js/mine.js") {}
    }
}
