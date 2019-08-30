package com.bitwiserain.pbbg.view.template

import com.bitwiserain.pbbg.db.model.User

data class MemberPageVM(
    val user: User,
    val battleUrl: String,
    val settingsUrl: String
)
