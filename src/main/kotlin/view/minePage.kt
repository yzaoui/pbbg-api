package miner.view

import io.ktor.html.Template
import kotlinx.html.*
import miner.view.model.ItemVM

fun minePage(homeURL: String, grid: List<List<ItemVM?>>): Template<HTML> = MainTemplate("Mine").apply {
    content {
        a(href = homeURL) {+"Return home"}
        table {
            id = "mining-grid"
            grid.forEach { row -> tr {
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
