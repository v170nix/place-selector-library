package net.arwix.placeselector.data

data class MapCameraPosition(
    val zoom: Float? = null,
    val bearing: Float? = null,
    val tilt: Float? = null,
    val targetLatitude: Double? = null,
    val targetLongitude: Double? = null,
)