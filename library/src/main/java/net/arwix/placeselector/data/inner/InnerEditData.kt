package net.arwix.placeselector.data.inner

import net.arwix.placeselector.data.MapCameraPosition
import net.arwix.placeselector.data.room.PlaceData
import java.time.ZoneId

data class InnerEditData(
    val id: Int? = null,
    val name: String? = null,
    val subName: String? = null,
    val zoneId: ZoneId? = null,
    val isAutoZone: Boolean = false,
    val latitude: Double,
    val longitude: Double,
    val cameraPosition: MapCameraPosition? = null,
    val isSelected: Boolean = false,
) {
    companion object {

        fun createFromPlaceData(data: PlaceData): InnerEditData {
            return InnerEditData(
                data.id,
                data.name,
                data.subName,
                data.zone,
                data.isAutoZone,
                data.latitude,
                data.longitude,
                MapCameraPosition(data.zoom, data.bearing, data.tilt),
                data.isSelected
            )
        }
    }
}