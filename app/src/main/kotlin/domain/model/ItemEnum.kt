package com.bitwiserain.pbbg.app.domain.model

enum class ItemEnum(val baseItem: BaseItem) {
    STONE(BaseItem.Material.Stone),
    COAL(BaseItem.Material.Coal),
    COPPER_ORE(BaseItem.Material.CopperOre),
    ICE_PICK(BaseItem.Pickaxe.IcePick),
    PLUS_PICKAXE(BaseItem.Pickaxe.PlusPickaxe),
    CROSS_PICKAXE(BaseItem.Pickaxe.CrossPickaxe),
    SQUARE_PICKAXE(BaseItem.Pickaxe.SquarePickaxe),
    APPLE_SAPLING(BaseItem.Sapling.AppleSapling),
    TOMATO_SEED(BaseItem.Seed.TomatoSeed),
    APPLE(BaseItem.Apple),
    TOMATO(BaseItem.Tomato)
}
