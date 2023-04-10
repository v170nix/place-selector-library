package net.arwix.placeselector.parts.edit.timezone.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.arwix.placeselector.common.ConflatedJob
import net.arwix.placeselector.common.except
import net.arwix.placeselector.common.mvi.SimpleViewModel
import net.arwix.placeselector.common.mvi.UISideEffect
import net.arwix.placeselector.data.inner.InnerEditDataHolder
import net.arwix.placeselector.data.timezone.TimeZoneDisplayEntry
import net.arwix.placeselector.data.timezone.TimeZoneRepository
import net.arwix.placeselector.parts.edit.timezone.ui.PlaceEditTimeZoneContract.*
import net.arwix.placeselector.parts.edit.timezone.ui.PlaceEditTimeZoneContract.State.*
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.CancellationException

class PlaceEditTimeZoneViewModel constructor(
    private val tzRepository: TimeZoneRepository,
    private val innerEditDataHolder: InnerEditDataHolder,
) : SimpleViewModel<Event, State, UISideEffect>(
    State(autoTimeZoneEntry = null)
) {

    private val autoLocationJob = ConflatedJob()
    private var saveToDbJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val list = tzRepository.getZones(Instant.now())
            reduceState {
                copy(listZones = list)
            }
            innerEditDataHolder.data
                .onEach { innerData ->
                    if (innerData == null) {
                        doEvent(Event.ClearData)
                    } else {
                        val selectedItem = State.SelectedItem.createSelectedItem(
                            innerData,
                            Instant.now()
                        )
                        reduceState {
                            copy(
                                selectedItem = selectedItem,
                                finishStepAvailable = selectedItem != null
                            )
                        }
                        updateAutoZone(
                            innerData.latitude,
                            innerData.longitude
                        )
                    }
                }
                .collect()
        }
    }

    override fun handleEvents(event: Event) {
        when (event) {
            Event.GetPremium -> {
                // TODO
            }
            is Event.SelectItem -> {
                reduceState {
                    copy(
                        selectedItem = event.item,
                        finishStepAvailable = true
                    )
                }
            }
            Event.Submit -> {
                val selectedItem = state.value.selectedItem ?: return
                if (saveToDbJob?.isActive == true) return
                saveToDbJob = viewModelScope.launch {
                    val isSuccess = when (selectedItem) {
                        is SelectedItem.FromAutoTimeZone -> {
                            innerEditDataHolder.updateTimeZone(
                                selectedItem.value.timeZoneDisplayEntry.id,
                                true
                            )
                        }
                        is SelectedItem.FromList -> {
                            innerEditDataHolder.updateTimeZone(selectedItem.value.id, false)
                        }
                    }
                    if (isSuccess) {
                        applyEffect {
                            Effect.OnSubmitData
                        }
                    }
                }
            }
            Event.ClearData -> {
                reduceState {
                    copy(
                        selectedItem = null,
                        finishStepAvailable = false
                    )
                }
            }
        }
    }

    @Suppress("MagicNumber")
    private fun updateAutoZone(latitude: Double, longitude: Double) {
        if (state.value.autoTimeZoneEntry == AutoTimeZoneEntry.Denied) return
        autoLocationJob += viewModelScope.launch {
            val autoZoneState = state.value.autoTimeZoneEntry
            if (autoZoneState is AutoTimeZoneEntry.Ok &&
                autoZoneState.latitude == latitude &&
                autoZoneState.longitude == longitude
            ) return@launch
            reduceState {
                val isSelectedItemFromAuto =
                    state.value.selectedItem is SelectedItem.FromAutoTimeZone
                copy(
                    autoTimeZoneEntry = AutoTimeZoneEntry.Loading(latitude, longitude),
                    selectedItem = if (isSelectedItemFromAuto) null else selectedItem,
                    finishStepAvailable = if (isSelectedItemFromAuto) false else finishStepAvailable
                )
            }
            delay(1000L)
            runCatching {
                tzRepository.getZoneId(latitude, longitude)
            }
                .except<CancellationException, ZoneId>()
                .onSuccess { zoneId ->
                    val currentInstant = Instant.now()
                    val entry = AutoTimeZoneEntry.Ok(
                        latitude, longitude,
                        TimeZoneDisplayEntry(zoneId, currentInstant)
                    )
                    reduceState {
                        copy(
                            autoTimeZoneEntry = entry,
                            selectedItem = selectedItem
                                ?: SelectedItem.FromAutoTimeZone(entry),
                            finishStepAvailable = true
                        )
                    }
                }
                .onFailure {
                    reduceState {
                        val isSelectedItemFromAuto =
                            state.value.selectedItem is SelectedItem.FromAutoTimeZone
                        copy(
                            autoTimeZoneEntry = AutoTimeZoneEntry.Error(
                                latitude, longitude, it
                            ),
                            selectedItem = if (isSelectedItemFromAuto) null else selectedItem,
                            finishStepAvailable = if (isSelectedItemFromAuto) false else finishStepAvailable
                        )
                    }
                }
        }
    }

}