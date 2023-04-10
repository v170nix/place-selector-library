package net.arwix.placeselector.parts.list.ui

import net.arwix.placeselector.common.mvi.UIEvent
import net.arwix.placeselector.common.mvi.UISideEffect
import net.arwix.placeselector.common.mvi.UIState
import net.arwix.placeselector.parts.list.data.PlaceListItemUI

interface PlaceListContract {
    data class State(
        val list: List<PlaceListItemUI>
    ) : UIState


    sealed class Event: UIEvent {
        object AddPlace: Event()
        object UpdateLocation: Event()
        data class UpdateLocationPermission(val isGrained: Boolean): Event()
        data class SelectItem(val item: PlaceListItemUI): Event()
        data class EditItem(val item: PlaceListItemUI.Custom): Event()
        data class DeleteItem(val item: PlaceListItemUI.Custom): Event()
        data class UndoDeleteItem(val item: PlaceListItemUI.Custom): Event()
    }

    sealed class Effect: UISideEffect {
        object ToEdit: Effect()
    }


}