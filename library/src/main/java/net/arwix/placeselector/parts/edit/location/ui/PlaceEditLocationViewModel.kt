package net.arwix.placeselector.parts.edit.location.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.arwix.placeselector.common.ConflatedJob
import net.arwix.placeselector.common.getSubTitle
import net.arwix.placeselector.common.getTitle
import net.arwix.placeselector.common.mvi.SimpleViewModel
import net.arwix.placeselector.data.GeocoderRepository
import net.arwix.placeselector.data.getPlaceInResult
import net.arwix.placeselector.data.inner.InnerEditDataHolder
import net.arwix.placeselector.parts.edit.location.ui.PlaceEditLocationContract.Effect
import net.arwix.placeselector.parts.edit.location.ui.PlaceEditLocationContract.Event
import net.arwix.placeselector.parts.edit.location.ui.PlaceEditLocationContract.Event.*
import net.arwix.placeselector.parts.edit.location.ui.PlaceEditLocationContract.State

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

open class PlaceEditLocationViewModel constructor(
    private val geocoderRepository: GeocoderRepository,
    private val innerEditDataHolder: InnerEditDataHolder
) : SimpleViewModel<Event, State, Effect>(
    State()
) {

    private val actionJob = ConflatedJob()
    private val geocodeJob = ConflatedJob()

    init {
        innerEditDataHolder.data
            .onEach { innerData ->
                if (innerData == null) {
                    doEvent(ClearData)
                } else {
                    reduceState {
                        copy(
                            inputState = State.InputState(
                                innerData.name ?: "",
                                innerData.subName ?: "",
                                innerData.latitude.toString(),
                                innerData.longitude.toString()
                            ),
                            cameraPosition = innerData.cameraPosition,
                            nextStepIsAvailable = true,
                        )
                    }
                    applyEffect {
                        Effect.ChangeLocationOnMap(
                            innerData.latitude,
                            innerData.longitude,
                            innerData.cameraPosition
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    @Suppress("ComplexMethod", "LongMethod")
    override fun handleEvents(event: Event) {
        when (event) {
            is SelectLocationFromMap -> {
                reduceState {
                    copy(
                        nextStepIsAvailable = true,
                        inputState = State.InputState(
                            "", "",
                            event.latitude.toString(),
                            event.longitude.toString()
                        ),
                        cameraPosition = event.cameraPosition
                    )
                }
                requestNames(event.latitude, event.longitude)
                applyEffect {
                    Effect.ChangeCenterMapToLatLng(event.latitude, event.longitude)
                }
            }
            is SelectLocationFromPOI -> {
                val name = event.point.name.takeWhile { it != "\n".toCharArray().first() }
                reduceState {
                    copy(
                        nextStepIsAvailable = true,
                        inputState = State.InputState(
                            name = name,
                            latitude = event.point.latitude.toString(),
                            longitude = event.point.longitude.toString()
                        )
                    )
                }
                requestNames(event.point.latitude, event.point.longitude, name)
                applyEffect {
                    Effect.ChangeCenterMapToLatLng(
                        event.point.latitude,
                        event.point.longitude
                    )
                }
            }
            is ChangeLatitudeFromInput -> {
                reduceState {
                    copy(inputState = inputState.copy(latitude = event.latitude))
                }
                doActionChangeLatitudeFromInput(event.latitude)
                state.value.inputState.getLatitude()?.let { lat ->
                    val lng = state.value.inputState.getLongitude() ?: return@let
                    applyEffect {
                        Effect.ChangeLocationOnMap(lat, lng, state.value.cameraPosition)
                    }
                }
            }
            is ChangeLongitudeFromInput -> {
                reduceState {
                    copy(inputState = inputState.copy(longitude = event.longitude))
                }
                doActionChangeLongitudeFromInput(event.longitude)
                state.value.inputState.getLongitude()?.let { lng ->
                    val lat = state.value.inputState.getLatitude() ?: return@let
                    applyEffect {
                        Effect.ChangeLocationOnMap(lat, lng, state.value.cameraPosition)
                    }
                }
            }
            is ChangeLocationFromPlace -> {
                val place = event.result.getPlaceInResult() ?: return

                geocodeJob.cancel()
                reduceState {
                    copy(
                        nextStepIsAvailable = true,
                        inputState = inputState.copy(
                            name = place.name.orEmpty(),
                            subName = place.subName,
                            latitude = place.latitude.toString(),
                            longitude = place.longitude.toString()
                        ),
                        cameraPosition = cameraPosition?.copy(zoom = 10f)
                    )
                }
                applyEffect {
                    Effect.ChangeLocationOnMap(
                        place.latitude,
                        place.longitude,
                        state.value.cameraPosition,
                        updateZoom = true
                    )
                }

            }
            ClearData -> {
                reduceState {
                    copy(
                        cameraPosition = null,
                        nextStepIsAvailable = false,
                        inputState = State.InputState()
                    )
                }
                innerEditDataHolder.clearData()
            }
            Submit -> {
                val name = state.value.inputState.name
                val subName = state.value.inputState.subName
                val latitude = state.value.inputState.latitude.toDoubleOrNull() ?: return
                val longitude = state.value.inputState.longitude.toDoubleOrNull() ?: return
                innerEditDataHolder.updateLocation(
                    name, subName, latitude, longitude, state.value.cameraPosition
                )
                applyEffect {
                    Effect.SubmitData
                }
            }
            is NotifyUpdateCameraPosition -> {
                reduceState {
                    copy(
                        cameraPosition = event.cameraPosition
                    )
                }
            }
        }
    }

    private fun doActionChangeLatitudeFromInput(inputLatitude: String?) {
        actionJob += viewModelScope.launch {
            geocodeJob.cancel()
            val longitude = state.value.inputState.longitude.toDoubleOrNull()
            val latitude = inputLatitude?.toDoubleOrNull()
            if (checkLatLng(latitude, longitude)) {
                reduceStateFromInputError()
            } else {
                reduceStateFromInput(latitude, longitude)
            }
        }
    }

    private fun doActionChangeLongitudeFromInput(inputLongitude: String?) {
        actionJob += viewModelScope.launch {
            geocodeJob.cancel()
            val longitude = inputLongitude?.toDoubleOrNull()
            val latitude = state.value.inputState.latitude.toDoubleOrNull()
            if (checkLatLng(latitude, longitude)) {
                reduceStateFromInputError()
            } else {
                reduceStateFromInput(latitude, longitude)
            }
        }
    }

    private fun requestNames(latitude: Double, longitude: Double, name: String? = null) {
        geocodeJob += viewModelScope.launch(Dispatchers.IO) {
            val address = geocoderRepository.getAddressOrNull(latitude, longitude)
            ensureActive()
            if (address == null) return@launch
            reduceState {
                copy(
                    nextStepIsAvailable = true,
                    inputState = inputState.copy(
                        name = name ?: address.getTitle(),
                        subName = address.getSubTitle()
                    )
                )
            }
        }
    }

    private fun reduceStateFromInputError() {
        reduceState {
            copy(
                nextStepIsAvailable = false,
                inputState = inputState.copy(name = "", subName = "")
            )
        }
    }

    private fun reduceStateFromInput(latitude: Double, longitude: Double) {
        reduceState {
            copy(
                nextStepIsAvailable = true,
                inputState = inputState.copy(
                    name = "",
                    subName = "",
                    latitude = latitude.toString(),
                    longitude = longitude.toString()
                )
            )
        }
        applyEffect {
            Effect.ChangeLocationOnMap(
                latitude,
                longitude,
                state.value.cameraPosition
            )
        }
        requestNames(latitude, longitude)
    }

    private companion object {

        @OptIn(ExperimentalContracts::class)
        private fun checkLatLng(latitude: Double?, longitude: Double?): Boolean {
            contract {
                returns(false) implies (latitude != null)
                returns(false) implies (longitude != null)
            }
            return latitude == null || latitude < -90.0 || latitude > 90.0 ||
                    longitude == null || longitude < -180.0 || longitude > 180.0
        }
    }

}

