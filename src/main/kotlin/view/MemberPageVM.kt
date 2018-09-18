package com.bitwiserain.pbbg.view

import com.bitwiserain.pbbg.db.model.User

data class MemberPageVM(
    val user: User,
    val home: ActionVM,
    val mine: ActionVM,
    val logout: ActionVM
)
