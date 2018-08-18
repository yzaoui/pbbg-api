package miner.view

import io.ktor.html.Template
import kotlinx.html.*
import miner.view.model.MineVM

fun minePageExistingMine(homeURL: String, mine: MineVM): Template<HTML> = MainTemplate("Mine").apply {
    content {
        a(href = homeURL) {+"Return home"}
        table {
            id = "mining-grid"
            mine.content.forEach { row -> tr {
                row.forEach { item -> td {
                    item?.let { style = "background-image: url('${it.imageURL}')" }
                } }
            } }
        }
        div {
            +"Equipped pickaxe: "
            span {
                id = "equipped-pickaxe"
                +"Loading..."
            }
        }
        script(src = "/js/mine.js") {}
    }
}

fun minePageNoMine(mineURL: String): Template<HTML> = MainTemplate("Mine").apply {
    content {
        form(action = mineURL, method = FormMethod.post) {
            button(type = ButtonType.submit) { +"Generate new mine" }
        }
    }
}
