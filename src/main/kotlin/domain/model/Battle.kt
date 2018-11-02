package com.bitwiserain.pbbg.domain.model

data class Battle(
    val allies: List<MyUnit>,
    val enemies: List<MyUnit>
)
