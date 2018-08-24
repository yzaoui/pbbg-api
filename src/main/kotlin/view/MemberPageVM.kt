package miner.view

import miner.data.model.User

data class MemberPageVM(val user: User, val home: ActionVM, val logout: ActionVM)
