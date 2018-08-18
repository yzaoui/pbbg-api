package miner.view

import io.ktor.html.Template
import kotlinx.html.FORM
import kotlinx.html.InputType
import kotlinx.html.input

class RegisterTemplate : Template<FORM> {
    override fun FORM.apply() {
        input(InputType.text)
        input(InputType.password)
    }
}
