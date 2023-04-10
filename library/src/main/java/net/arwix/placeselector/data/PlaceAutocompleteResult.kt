package net.arwix.placeselector.data

import net.arwix.placeselector.MapPlace

sealed class PlaceAutocompleteResult {
    object Canceled : PlaceAutocompleteResult()
    data class Error<T>(val message: T) : PlaceAutocompleteResult()
    data class Ok(val place: MapPlace) : PlaceAutocompleteResult()
}

fun PlaceAutocompleteResult.getPlaceInResult(): MapPlace? {
    if (this is PlaceAutocompleteResult.Ok) return place
    return null
}