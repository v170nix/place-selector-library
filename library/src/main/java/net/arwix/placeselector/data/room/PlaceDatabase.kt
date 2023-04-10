package net.arwix.placeselector.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PlaceData::class],
    version = 1,
    exportSchema = false
)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun getPlaceDao(): PlaceDao
}