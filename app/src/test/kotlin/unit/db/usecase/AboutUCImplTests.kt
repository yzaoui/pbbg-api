package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.APP_VERSION
import com.bitwiserain.pbbg.app.APP_VERSIONS
import com.bitwiserain.pbbg.app.db.usecase.AboutUCImpl
import com.bitwiserain.pbbg.app.domain.usecase.AboutUC
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AboutUCImplTests {
    private val aboutUC: AboutUC = AboutUCImpl()

    @Test
    fun `When requesting app version, latest app version should return`() {
        assertEquals(APP_VERSION, aboutUC.getAppVersion())
    }

    @Test
    fun `When requesting patch notes, all patch notes should return`() {
        assertEquals(APP_VERSIONS.size, aboutUC.getPatchNotes().size, "There should be one set of patch notes per app version.")
    }
}
