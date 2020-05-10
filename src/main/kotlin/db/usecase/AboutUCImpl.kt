package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.APP_VERSION
import com.bitwiserain.pbbg.PatchNotesHelper
import com.bitwiserain.pbbg.domain.usecase.AboutUC

class AboutUCImpl : AboutUC {
    override fun getAppVersion(): String = APP_VERSION

    override fun getPatchNotes(): List<String> = PatchNotesHelper.getAll()
}
