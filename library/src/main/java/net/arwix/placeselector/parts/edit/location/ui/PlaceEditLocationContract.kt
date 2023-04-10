package net.arwix.placeselector.parts.edit.location.ui

import net.arwix.placeselector.common.mvi.UIEvent
import net.arwix.placeselector.common.mvi.UISideEffect
import net.arwix.placeselector.common.mvi.UIState
import net.arwix.placeselector.data.MapCameraPosition
import net.arwix.placeselector.data.MapPointOfInterest
import net.arwix.placeselector.data.PlaceAutocompleteResult


object PlaceEditLocationContract {

    data class State(
        val cameraPosition: MapCameraPosition? = null,
        val nextStepIsAvailable: Boolean = false,
        val inputState: InputState = InputState()
    ) : UIState {
        data class InputState(
            val name: String = "",
            val subName: String = "",
            val latitude: String = "",
            val longitude: String = ""
        ) {

            fun getLatitude(): Double? {
                val lat = latitude.toDoubleOrNull() ?: return null
                if (lat < -90.0 || lat > 90.0) return null
                return lat
            }

            fun getLongitude(): Double? {
                val lng = longitude.toDoubleOrNull() ?: return null
                if (lng < -180.0 || lng > 180.0) return null
                return lng
            }
        }
    }

    sealed class Event : UIEvent {
        data class SelectLocationFromMap(
            val latitude: Double,
            val longitude: Double,
            val cameraPosition: MapCameraPosition?
        ) : Event()

        data class SelectLocationFromPOI(
            val point: MapPointOfInterest,
            val cameraPosition: MapCameraPosition?
        ) : Event()

        data class NotifyUpdateCameraPosition(val cameraPosition: MapCameraPosition) : Event()
        data class ChangeLatitudeFromInput(val latitude: String) : Event()
        data class ChangeLongitudeFromInput(val longitude: String) : Event()
        data class ChangeLocationFromPlace(val result: PlaceAutocompleteResult) : Event()

        object Submit : Event()
        object ClearData : Event()
    }

    sealed class Effect : UISideEffect {
        data class ChangeLocationOnMap(
            val latitude: Double,
            val longitude: Double,
            val cameraPosition: MapCameraPosition?,
            val updateZoom: Boolean = false,
        ) : Effect()

        data class ChangeCenterMapToLatLng(val latitude: Double, val longitude: Double) : Effect()
        object SubmitData : Effect()
    }

}
