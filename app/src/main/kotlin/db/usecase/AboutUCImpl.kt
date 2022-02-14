package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.APP_VERSION
import com.bitwiserain.pbbg.app.PatchNotesHelper
import com.bitwiserain.pbbg.app.domain.usecase.AboutUC

class AboutUCImpl : AboutUC {
    override fun getAppVersion(): String = APP_VERSION

    override fun getPatchNotes(): List<String> = PatchNotesHelper.getAll()
}
