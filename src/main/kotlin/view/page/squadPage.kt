package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.SiteSection
import com.bitwiserain.pbbg.view.template.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.link
import kotlinx.html.script

fun squadPage(memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Squad", memberPageVM, SiteSection.SQUAD).apply {
    headContent {
        link(href = "/css/squad.css", rel = "stylesheet")
    }
    endOfBody {
        script(src = "/js/squad.js") {}
    }
}
