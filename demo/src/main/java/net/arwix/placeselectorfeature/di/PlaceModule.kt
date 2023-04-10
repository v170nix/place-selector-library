package net.arwix.placeselectorfeature.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.arwix.placeselector.data.GeocoderRepository
import net.arwix.placeselector.data.inner.InnerEditDataHolder
import net.arwix.placeselector.data.room.PlaceDao
import net.arwix.placeselector.data.room.PlaceDatabase
import net.arwix.placeselector.parts.list.domain.PlaceListUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaceModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): PlaceDatabase {
        return Room.databaseBuilder(
            context,
            PlaceDatabase::class.java, "place-demo-db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideDao(db: PlaceDatabase): PlaceDao {
        return db.getPlaceDao()
    }

    @Provides
    fun provideGeocoderRepository(@ApplicationContext context: Context): GeocoderRepository =
        GeocoderRepository(context)

    @Provides
    @Singleton
    fun providePlaceListUseCase(
        @ApplicationContext context: Context,
        dao: PlaceDao,
        geocoder: GeocoderRepository,
    ): PlaceListUseCase = PlaceListUseCase(
        context = context,
        dao = dao,
        geocoder = geocoder,
        getLocation = {
            null
        }
    )

    @Provides
    @Singleton
    fun provideInnerEditDataHolder(
        dao: PlaceDao,
    ): InnerEditDataHolder = InnerEditDataHolder(dao)
}