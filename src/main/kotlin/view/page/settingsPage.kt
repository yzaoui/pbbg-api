package com.bitwiserain.pbbg.view.page

import com.bitwiserain.pbbg.PASSWORD_REGEX
import com.bitwiserain.pbbg.PASSWORD_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.view.SiteSection
import com.bitwiserain.pbbg.view.template.MemberPageVM
import com.bitwiserain.pbbg.view.template.MemberTemplate
import io.ktor.html.Template
import kotlinx.html.*

private const val currentPasswordId = "current-password"
private const val newPasswordId = "new-password"
private const val confirmNewPasswordId = "confirm-new-password"

fun settingsPage(memberPageVM: MemberPageVM, changePasswordUrl: String, error: String? = null): Template<HTML> = MemberTemplate("Settings", memberPageVM, SiteSection.SETTINGS).apply {
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
                    required = true
                }

                br { }

                label {
                    htmlFor = newPasswordId
                    +"New Password:"
                }
                input(type = InputType.password, name = "newPassword") {
                    id = newPasswordId
                    required = true
                    pattern = PASSWORD_REGEX
                    title = PASSWORD_REGEX_DESCRIPTION
                }

                br { }

                label {
                    htmlFor = confirmNewPasswordId
                    +"Confirm New Password:"
                }
                input(type = InputType.password, name = "confirmNewPassword") {
                    id = confirmNewPasswordId
                    required = true
                    pattern = PASSWORD_REGEX
                    title = PASSWORD_REGEX_DESCRIPTION
                }

                br { }

                input(type = InputType.submit) {
                    value = "Change"
                }
            }
        }
    }
}
