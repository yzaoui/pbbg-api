package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.farm.Plot

interface FarmUC {
    /**
     * Gets the user's current plots.
     */
    fun getPlots(userId: Int): List<Plot>

    /**
     * Plants a plantable item in the user's given plot.
     *
     * @return The updated plot with a plantable entity in it.
     *
     * @throws UserPlotNotFoundException when this user-plot combination is not found.
     * @throws OccupiedPlotException when this plot is already occupied.
     * @throws InventoryItemNotFoundException when this user-item combination is not found.
     * @throws InsufficientItemQuantity when there aren't enough of this item to plant.
     * @throws ItemNotPlantableException when this item is not plantable.
     */
    fun plant(userId: Int, plotId: Long, itemId: Long): Plot

    /**
     * Harvests from a plantable entity from the user's given plot.
     *
     * @return The updated plot with a harvested plantable entity in it.
     *
     * @throws UserPlotNotFoundException when this user-plot combination is not found.
     * @throws EmptyPlotException when this plot does not have a plant in it.
     * @throws PlantNotHarvestableException when this plot's plant is not ready for harvest.
     */
    fun harvest(userId: Int, plotId: Long): Plot

    /**
     * Expands number of plots by one.
     *
     * @return The new plot.
     */
    fun expand(userId: Int): Plot

    /**
     * Reorders plots by moving one by its [plotId] to a given [targetIndex].
     *
     * @return The updated list of the user's plots.
     */
    fun reorder(userId: Int, plotId: Long, targetIndex: Int): List<Plot>
}

class UserPlotNotFoundException : Exception()
class OccupiedPlotException : Exception()
class EmptyPlotException : Exception()
class InsufficientItemQuantity : Exception()
class ItemNotPlantableException : Exception()
class PlantNotHarvestableException : Exception()
