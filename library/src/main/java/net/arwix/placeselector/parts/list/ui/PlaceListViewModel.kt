package net.arwix.placeselector.parts.list.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.arwix.placeselector.common.ConflatedJob
import net.arwix.placeselector.common.mvi.SimpleViewModel
import net.arwix.placeselector.data.inner.InnerEditDataHolder
import net.arwix.placeselector.parts.list.data.PlaceListItemUI
import net.arwix.placeselector.parts.list.domain.PlaceListUseCase
import net.arwix.placeselector.parts.list.ui.PlaceListContract.*

@Suppress("MemberVisibilityCanBePrivate")
open class PlaceListViewModel constructor(
    protected val placeListUseCase: PlaceListUseCase,
    protected val innerEditDataHolder: InnerEditDataHolder
) : SimpleViewModel<Event, State, Effect>(
    State(listOf())
) {
    private val locationUpdateJob = ConflatedJob()

    // https://stackoverflow.com/questions/64721218/jetpack-compose-launch-activityresultcontract-request-from-composable-function
    // https://ngengesenior.medium.com/pick-image-from-gallery-in-jetpack-compose-5fa0d0a8ddaf

    init {
        placeListUseCase.places.map {
            reduceState {
                copy(list = it)
            }
        }.launchIn(viewModelScope)
    }

    override fun handleEvents(event: Event) {
        when (event) {
            Event.AddPlace -> {
                innerEditDataHolder.clearData()
                applyEffect {
                    Effect.ToEdit
                }
            }
            Event.UpdateLocation -> {
                doLocationUpdate(true)
            }
            is Event.UpdateLocationPermission -> {
                if (event.isGrained) doLocationUpdate()
            }
            is Event.SelectItem -> {
                viewModelScope.launch {
                    when (event.item) {
                        is PlaceListItemUI.Auto -> {
                            placeListUseCase.selectAutoItem()
                        }
                        is PlaceListItemUI.Custom -> {
                            placeListUseCase.selectCustomItem(event.item.place)
                        }
                    }
                }
            }
            is Event.EditItem -> {
                innerEditDataHolder.editData(event.item.place)
                applyEffect {
                    Effect.ToEdit
                }
            }
            is Event.DeleteItem -> {
                viewModelScope.launch {
                    placeListUseCase.deleteItem(event.item.place.id)
                }
            }
            is Event.UndoDeleteItem -> {
                viewModelScope.launch {
                    placeListUseCase.undoDeleteItem(event.item)
                }
            }
        }
    }

    private fun doLocationUpdate(isForceUpdateLocation: Boolean = false) {
        locationUpdateJob += viewModelScope.launch {
            placeListUseCase.requestUpdateAutoLocation(isForceUpdateLocation)
        }
    }

}