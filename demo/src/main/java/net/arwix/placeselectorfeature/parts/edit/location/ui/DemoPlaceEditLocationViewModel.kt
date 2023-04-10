package net.arwix.placeselectorfeature.parts.edit.location.ui

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import net.arwix.placeselector.data.GeocoderRepository
import net.arwix.placeselector.data.inner.InnerEditDataHolder
import net.arwix.placeselector.parts.edit.location.ui.PlaceEditLocationViewModel
import javax.inject.Inject

@HiltViewModel
class DemoPlaceEditLocationViewModel @Inject constructor(
    geocoderRepository: GeocoderRepository,
    innerEditDataHolder: InnerEditDataHolder
) :
    PlaceEditLocationViewModel(geocoderRepository, innerEditDataHolder) {

        init {
            Log.e("log", "init edit location view model")
        }

}