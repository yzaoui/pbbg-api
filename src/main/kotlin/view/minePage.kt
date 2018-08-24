package miner.view

import io.ktor.html.Template
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.link
import kotlinx.html.script
import miner.view.template.MemberTemplate

fun minePage(homeURL: String, memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Mine", memberPageVM).apply {
    headContent {
        link(href = "/css/mine.css", rel = "stylesheet")
    }
    content {
        a(href = homeURL) {+"Return home"}
    }
    endOfBody {
        script(src = "/js/mine.js") {}
    }
}
