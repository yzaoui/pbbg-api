package com.bitwiserain.pbbg.domain.usecase

interface AboutUC {
    fun getAppVersion(): String
    fun getPatchNotes(): List<String>
}
