package com.bitwiserain.pbbg.app.test.db

import com.bitwiserain.pbbg.app.db.Transaction

object TestTransaction : Transaction {
    override fun <T> invoke(block: () -> T): T = block()
}
