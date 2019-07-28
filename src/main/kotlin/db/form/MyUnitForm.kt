package com.bitwiserain.pbbg.db.form

import com.bitwiserain.pbbg.domain.model.MyUnitEnum

/**
 * The form of fields required to create a new unit.
 * The new unit will start 0 with exp, and be at full HP.
 */
data class MyUnitForm(
    val enum: MyUnitEnum,
    val hp: Int,
    val atk: Int,
    val def: Int
)
