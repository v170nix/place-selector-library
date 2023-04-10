package net.arwix.placeselector.data.timezone

import java.time.Instant
import java.time.ZoneId

interface TimeZoneRepository  {
    fun getZones(instant: Instant): List<TimeZoneDisplayEntry>
    suspend fun getZoneId(latitude: Double, longitude: Double): ZoneId
}