package net.arwix.placeselector.data.inner

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.arwix.placeselector.data.MapCameraPosition
import net.arwix.placeselector.data.room.PlaceDao
import net.arwix.placeselector.data.room.PlaceData
import java.time.ZoneId

class InnerEditDataHolder constructor(private val dao: PlaceDao) {
    private val _state = MutableStateFlow<InnerEditData?>(null)
    val data = _state.asStateFlow()

    fun clearData() {
        _state.value = null
    }

    fun editData(placeData: PlaceData) {
        _state.value = InnerEditData.createFromPlaceData(placeData)
    }

    fun updateLocation(
        name: String,
        subName: String,
        latitude: Double,
        longitude: Double,
        cameraPosition: MapCameraPosition?
    ) {
        _state.update {
            it?.copy(
                name = name,
                subName = subName,
                latitude = latitude,
                longitude = longitude,
                cameraPosition = cameraPosition
            ) ?: InnerEditData(
                null, name, subName,
                latitude = latitude,
                longitude = longitude,
                cameraPosition = cameraPosition
            )
        }
    }

    suspend fun updateTimeZone(
        zoneId: ZoneId,
        isAutoZone: Boolean,
    ): Boolean {
        val value = _state.value ?: return false
        _state.value = value.copy(zoneId = zoneId, isAutoZone = isAutoZone)
        return submit()
    }

    private suspend fun submit(): Boolean {
        val value = _state.value ?: return false
        value.zoneId ?: return false
        val place = PlaceData(
            id = value.id,
            name = value.name,
            subName = value.subName,
            latitude = value.latitude,
            longitude = value.longitude,
            zone = value.zoneId,
            isAutoZone = value.isAutoZone,
            zoom = value.cameraPosition?.zoom,
            bearing = value.cameraPosition?.bearing,
            tilt = value.cameraPosition?.tilt,
            isSelected = value.isSelected,
            isAutoLocation = false
        )
        if (place.id == null) {
            dao.insert(place)
        } else {
            val listIsNotSelectedItems = dao.getSelectedItem() == null
            if (listIsNotSelectedItems) {
                dao.update(place.copy(isSelected = true))
            } else dao.update(place)
        }
        return true
    }

}