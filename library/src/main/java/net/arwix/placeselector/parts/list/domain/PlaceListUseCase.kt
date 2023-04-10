package net.arwix.placeselector.parts.list.domain

import android.content.Context
import android.location.Location
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.arwix.placeselector.common.ConflatedJob
import net.arwix.placeselector.common.locationCheckPermission
import net.arwix.placeselector.data.GeocoderRepository
import net.arwix.placeselector.data.room.PlaceDao
import net.arwix.placeselector.data.room.PlaceData
import net.arwix.placeselector.parts.list.data.PlaceListItemUI
import java.time.ZoneId

class PlaceListUseCase constructor(
    context: Context,
    private val dao: PlaceDao,
    private val geocoder: GeocoderRepository,
    private val getLocation: suspend (isForceUpdate: Boolean) -> Location?
) {

    private val applicationContext = context.applicationContext
    private val geolocationJob = ConflatedJob()

    private val innerAutoState = MutableStateFlow<PlaceListItemUI.Auto?>(null)
    private val dbPlaces = dao.getAll()
        .map { list -> list.map(::transformToUI) }
        .withIndex()

    val places = dbPlaces
        .combine(innerAutoState) { (index, list: List<PlaceListItemUI>), innerAutoItemUI ->
            val autoItem = list.getAutoItem()
            if (autoItem != null) {
                if (index == 0) updateCurrentLocation()
                list
            } else {
                list.toMutableList().apply {
                    if (innerAutoItemUI != null) {
                        add(0, innerAutoItemUI)
                    } else {
                        innerAutoState.value = createAutoItem()
                    }
                }
            }
        }
        .filter { list -> list.firstOrNull { it is PlaceListItemUI.Auto } != null }

    suspend fun deleteItem(id: Int?) {
        if (id != null) dao.deleteById(id)
    }

    suspend fun undoDeleteItem(item: PlaceListItemUI.Custom) {
        dao.insert(item.place)
    }

    suspend fun selectAutoItem() {
        dao.selectAutoItem()
    }

    suspend fun selectCustomItem(place: PlaceData) {
        dao.selectCustomItem(place.copy(isSelected = true))
    }

    suspend fun requestUpdateAutoLocation(isForceUpdateLocation: Boolean = false) {
        innerAutoState.value = createAutoItem(isForceUpdateLocation)
    }

    @Suppress("MagicNumber")
    private suspend fun createAutoItem(isForceUpdateLocation: Boolean = false): PlaceListItemUI.Auto? {
        val permission = applicationContext.locationCheckPermission()

        var location: Location? = null
        val timeOut = 3000L
        val delta = 1000L
        val maxCount = 5
        var count = 0

        while (location == null && count < maxCount) {
            count++
            location = withTimeoutOrNull(timeOut + delta * count) {
                getLocation(isForceUpdateLocation)
            }
            if (location == null) location = getLocation(false)
        }

        return if (location == null) {
            if (permission) PlaceListItemUI.Auto(PlaceListItemUI.Auto.State.Allow(null))
            else PlaceListItemUI.Auto(PlaceListItemUI.Auto.State.Denied)
        } else {
            saveCurrentLocation(location)
            null
        }
    }

    private suspend fun updateCurrentLocation() {
        coroutineScope {
            launch {
                val location = getLocation(false) ?: return@launch
                saveCurrentLocation(location)
            }
        }
    }

    private suspend fun saveCurrentLocation(location: Location) {
        dao.updateAutoItem(location, ZoneId.systemDefault())
        coroutineScope {
            geolocationJob += launch(Dispatchers.IO, CoroutineStart.LAZY) geo@{
                val address =
                    geocoder.getAddressOrNull(location.latitude, location.longitude) ?: return@geo
                ensureActive()
                dao.updateAutoItem(address)
            }
            geolocationJob.start()
        }
    }

    private companion object {
        private fun transformToUI(placeData: PlaceData) =
            if (!placeData.isAutoLocation) PlaceListItemUI.Custom(placeData)
            else PlaceListItemUI.Auto(PlaceListItemUI.Auto.State.Allow(placeData))

        private fun List<PlaceListItemUI>.getAutoItem() = find { it is PlaceListItemUI.Auto }

    }
}
