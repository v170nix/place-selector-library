package net.arwix.placeselector.data

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.annotation.WorkerThread
import java.io.IOException
import java.util.*

class GeocoderRepository constructor(context: Context) {

    private val geocoder: Geocoder? = if (Geocoder.isPresent()) {
        Geocoder(context.applicationContext, Locale.getDefault())
    } else null


    @WorkerThread
    @Throws(IOException::class)
    fun getAddress(latitude: Double, longitude: Double): Address? {
        return geocoder?.getFromLocation(latitude, longitude, 1)?.getOrNull(0)
    }

    fun getAddressOrNull(latitude: Double, longitude: Double): Address? =
        runCatching { getAddress(latitude, longitude) }.getOrNull()
}