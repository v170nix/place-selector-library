package net.arwix.placeselector.common

import android.location.Address

fun Address.getTitle(): String {
    return premises
        ?: thoroughfare?.let { name ->
            if (name == "Unnamed Road") return@let null
            runCatching {
                Integer.parseInt(name)
                null
            }.getOrElse { name }
        }
        ?: locality
        ?: subAdminArea
        ?: adminArea
        ?: countryName
        ?: ""
}

fun Address.getSubTitle(): String {
    val placeName = getTitle()
    val shortCountry = countryCode
    val level1 = adminArea
    val locality = this.locality
    val list = ArrayList<String>()
    if (locality != null && locality != placeName) list += locality
    if (level1 != null && level1 != placeName) list += level1
    if (shortCountry != null && shortCountry != placeName) list += shortCountry
    return list.joinToString()
}