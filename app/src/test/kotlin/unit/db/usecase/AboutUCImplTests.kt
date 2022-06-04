package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.APP_VERSION
import com.bitwiserain.pbbg.app.APP_VERSIONS
import com.bitwiserain.pbbg.app.db.usecase.AboutUCImpl
import com.bitwiserain.pbbg.app.domain.usecase.AboutUC
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AboutUCImplTests {
    private val aboutUC: AboutUC = AboutUCImpl()

    @Test
    fun `When requesting app version, latest app version should return`() {
        aboutUC.getAppVersion() shouldBe APP_VERSION
    }

    @Test
    fun `When requesting patch notes, all patch notes should return`() {
        withClue("There should be one set of patch notes per app version.") {
            aboutUC.getPatchNotes() shouldBeSameSizeAs APP_VERSIONS
        }
    }
}
