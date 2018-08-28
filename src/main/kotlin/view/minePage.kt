package pbbg.view

import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.link
import kotlinx.html.script
import pbbg.view.template.MemberTemplate

fun minePage(memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Mine", memberPageVM).apply {
    headContent {
        link(href = "/css/mine.css", rel = "stylesheet")
    }
    endOfBody {
        script(src = "/js/mine.js") {}
    }
}
