package com.bitwiserain.pbbg.app.domain.usecase

interface AboutUC {
    fun getAppVersion(): String
    fun getPatchNotes(): List<String>
}
