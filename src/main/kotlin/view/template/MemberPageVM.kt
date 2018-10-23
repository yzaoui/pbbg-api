package com.bitwiserain.pbbg.view.template

import com.bitwiserain.pbbg.db.model.User

data class MemberPageVM(
    val user: User,
    val homeUrl: String,
    val inventoryUrl: String,
    val mineUrl: String,
    val settingsUrl: String,
    val logoutUrl: String
)
