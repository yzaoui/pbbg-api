package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.itemdetails.ItemDetails

interface ItemUC {
    /**
     * A given item's details.
     *
     * @throws ItemNotFoundException when this item ID does not exist
     */
    fun getItemDetails(itemId: Long): ItemDetails
}

class ItemNotFoundException(val itemId: Long) : Exception()
