package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.view.template.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.*

const val currentPasswordId = "current-password"
const val newPasswordId = "new-password"
const val confirmNewPasswordId = "confirm-new-password"

fun settingsPage(memberPageVM: MemberPageVM, changePasswordUrl: String, error: String? = null): Template<HTML> = MemberTemplate("Settings", memberPageVM).apply {
    content {
        if (error != null) {
            div(classes = "alert-error") {
                +error
            }
        }
        form(action = changePasswordUrl, method = FormMethod.post) {
            fieldSet {
                legend { +"Change password" }

                label {
                    htmlFor = currentPasswordId
                    +"Current Password:"
                }
                input(type = InputType.password, name = "currentPassword") {
                    id = currentPasswordId
                }

                br { }

                label {
                    htmlFor = newPasswordId
                    +"New Password:"
                }
                input(type = InputType.password, name = "newPassword") {
                    id = newPasswordId
                }

                br { }

                label {
                    htmlFor = confirmNewPasswordId
                    +"Confirm New Password:"
                }
                input(type = InputType.password, name = "confirmNewPassword") {
                    id = confirmNewPasswordId
                }

                br { }

                input(type = InputType.submit) {
                    value = "Change"
                }
            }
        }
    }
}
