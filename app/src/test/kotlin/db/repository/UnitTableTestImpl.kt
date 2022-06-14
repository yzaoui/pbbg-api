package com.bitwiserain.pbbg.app.test.db.repository

import com.bitwiserain.pbbg.app.db.repository.UnitTable
import com.bitwiserain.pbbg.app.domain.model.MyUnit
import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum

class UnitTableTestImpl(private val units: MutableMap<Long, MyUnit> = mutableMapOf()) : UnitTable {
    override fun insertUnitAndGetId(unit: UnitTable.UnitForm): Long {
        val id = units.size.toLong()
        val unitEnum = unit.enum
        val hp = unit.hp
        val maxHP = unit.hp
        val atk = unit.atk
        val def = unit.def
        val int = unit.int
        val res = unit.res
        val exp = 0L

        units[id] = when (unitEnum) {
            MyUnitEnum.ICE_CREAM_WIZARD -> MyUnit.IceCreamWizard(id, hp, maxHP, atk, def, int, res, exp)
            MyUnitEnum.TWOLIP -> MyUnit.Twolip(id, hp, maxHP, atk, def, int, res, exp)
            MyUnitEnum.CARPSHOOTER -> MyUnit.Carpshooter(id, hp, maxHP, atk, def, int, res, exp)
            MyUnitEnum.FLAMANGO -> MyUnit.Flamango(id, hp, maxHP, atk, def, int, res, exp)
        }

        return id
    }

    override fun updateUnit(unitId: Long, unit: MyUnit) {
        units[unitId] = unit
    }

    override fun getUnit(unitId: Long): MyUnit? = units[unitId]
}
