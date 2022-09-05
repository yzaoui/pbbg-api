package com.bitwiserain.pbbg.app.test

import com.bitwiserain.pbbg.app.reorder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ListReorderTest {

    @Test
    @DisplayName("List.reorder should work when moving to a larger index")
    fun moveToLarger() {
        listOf(1, 2, 3, 4, 5).reorder(0, 3) shouldBe listOf(2, 3, 4, 1, 5)
        listOf(1, 2, 3, 4, 5).reorder(0, 4) shouldBe listOf(2, 3, 4, 5, 1)
        listOf(1, 2).reorder(0, 1) shouldBe listOf(2, 1)
    }

    @Test
    @DisplayName("List.reorder should work when moving to a smaller index")
    fun moveToSmaller() {
        listOf(1, 2, 3, 4, 5).reorder(3, 0) shouldBe listOf(4, 1, 2, 3, 5)
        listOf(1, 2, 3, 4, 5).reorder(4, 0) shouldBe listOf(5, 1, 2, 3, 4)
        listOf(1, 2).reorder(1, 0) shouldBe listOf(2, 1)
    }

    @Test
    @DisplayName("List.reorder should create a new instance even if there is nothing to reorder")
    fun newInstance() {
        val list = listOf(1, 2, 3, 4, 5)
        val reorderedList = list.reorder(2, 2)

        reorderedList shouldNotBeSameInstanceAs list
        reorderedList shouldBe list
    }
}
