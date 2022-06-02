package com.bitwiserain.pbbg.app.db

interface Transaction {
    operator fun <T> invoke(block: () -> T): T
}
