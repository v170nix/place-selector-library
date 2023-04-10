package net.arwix.placeselector.data.room

import android.location.Address
import android.location.Location
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.arwix.placeselector.common.getSubTitle
import net.arwix.placeselector.common.getTitle
import java.time.ZoneId

@Suppress("TooManyFunctions")
@Dao
abstract class PlaceDao {
    @Query("SELECT * FROM location_tz_table ORDER BY (id = 1) DESC, id DESC")
    abstract fun getAll(): Flow<List<PlaceData>>

    @Query("SELECT * FROM location_tz_table WHERE id = :id LIMIT 1")
    abstract suspend fun getItem(id: Int): PlaceData?

    @Query("SELECT * FROM location_tz_table WHERE isAutoLocation = 1 LIMIT 1")
    abstract suspend fun getAutoItem(): PlaceData?

    @Query("SELECT * FROM location_tz_table WHERE isAutoLocation = 1 LIMIT 1")
    abstract fun getAutoItemAsFlow(): Flow<PlaceData?>

    @Query("SELECT * FROM location_tz_table WHERE isSelected = 1 LIMIT 1")
    abstract suspend fun getSelectedItem(): PlaceData?

    @Query("SELECT * FROM location_tz_table WHERE isSelected = 1 LIMIT 1")
    abstract fun getSelectedItemAsFlow(): Flow<PlaceData?>

    @Transaction
    open suspend fun selectCustomItem(data: PlaceData) {
        allDeselect()
        update(data)
    }

    @Transaction
    open suspend fun updateAutoItem(location: Location, zoneId: ZoneId) {
        val autoItem = getAutoItem()
        if (autoItem != null) {
            update(
                autoItem.copy(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    zone = zoneId
                )
            )
        } else {
            val isSelected = getSelectedItem() == null
            insert(
                PlaceData(
                    id = null,
                    name = null,
                    subName = null,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    zone = zoneId,
                    isAutoLocation = true,
                    isSelected = isSelected
                )
            )
        }
    }

    @Transaction
    open suspend fun updateAutoItem(address: Address) {
        val autoItem = getAutoItem()
        if (autoItem != null) {
            update(
                autoItem.copy(
                    name = address.getTitle(),
                    subName = address.getSubTitle()
                )
            )
        }
    }

    @Transaction
    open suspend fun selectAutoItem() {
        allDeselect()
        selectAuto()
    }

    @Insert
    abstract suspend fun insert(data: PlaceData)

    @Update
    abstract suspend fun update(data: PlaceData)

    @Query("DELETE FROM location_tz_table WHERE id = :id")
    abstract suspend fun deleteById(id: Int)

    @Query("UPDATE location_tz_table SET isSelected = 1 WHERE isAutoLocation = 1")
    protected abstract suspend fun selectAuto()

    @Query("UPDATE location_tz_table SET isSelected = 0")
    protected abstract suspend fun allDeselect()

}