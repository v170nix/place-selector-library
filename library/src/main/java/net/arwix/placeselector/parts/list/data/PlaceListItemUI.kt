package net.arwix.placeselector.parts.list.data

import net.arwix.placeselector.data.room.PlaceData


sealed class PlaceListItemUI(val isSelected: Boolean, val isSelectable: Boolean) {

    data class Auto(val state: State) : PlaceListItemUI(
        isSelected = run {
            if (state is State.Allow && state.data != null) state.data.isSelected else false
        },
        isSelectable = state is State.Allow && state.data != null
    ) {
        sealed class State {
            object None : State()
            object Denied : State()
            object DeniedRationale : State()
            data class Allow(val data: PlaceData?) : State()
        }
    }

    data class Custom(val place: PlaceData) : PlaceListItemUI(place.isSelected, true)

    fun getKey(): Int {
        return when (this) {
            is Auto -> -1
            is Custom -> place.id ?: Int.MAX_VALUE
        }
    }

}

