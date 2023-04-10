package net.arwix.placeselector.data.room

import androidx.room.*
import java.time.ZoneId

@Entity(tableName = "location_tz_table")
@TypeConverters(PlaceData.ZoneConverters::class)
data class PlaceData(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val name: String?,
    @ColumnInfo(name = "sub_name") val subName: String?,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val zone: ZoneId,
    val isAutoZone: Boolean = false,
    val zoom: Float? = null,
    val bearing: Float? = null,
    val tilt: Float? = null,
    val isSelected: Boolean = false,
    val isAutoLocation: Boolean = false
) {

    internal class ZoneConverters {

        @TypeConverter
        fun zoneIdToStr(zone: ZoneId?): String? = zone?.id

        @TypeConverter
        fun strToZoneId(string: String?): ZoneId? =
            string.runCatching { ZoneId.of(this) }.getOrNull()
    }
}