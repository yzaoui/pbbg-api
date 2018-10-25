package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.template.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.HTML

fun squadPage(memberPageVM: MemberPageVM): Template<HTML> = MemberTemplate("Squad", memberPageVM).apply {

}
