package net.arwix.placeselector.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlin.math.abs

fun Context.locationCheckPermission() = ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_FINE_LOCATION
) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_COARSE_LOCATION
) == PackageManager.PERMISSION_GRANTED

private const val LOCATION_FORMAT_STRING = "%02d°%02d′%04.1f″"

@Suppress("MagicNumber")
private fun getDegree(double: Double): Triple<Int, Int, Double> {
    val degs = abs(double)
    val deg = degs.toInt()
    val minutes = (degs - deg) * 60.0
    val minute = minutes.toInt()
    val second = ((minutes - minute) * 60.0)
    return Triple(deg, minute, second)
}

@Suppress("SpreadOperator")
fun latToString(latitude: Double, n: String, s: String): String {
    val ns = if (latitude > 0) n else s
    val lat = getDegree(latitude).toList().toTypedArray()
    return buildString {
        append(String.format(LOCATION_FORMAT_STRING, *lat))
        append(ns)
    }
}

fun lngToString(longitude: Double, e: String, w: String): String {
    return latToString(longitude, e, w)
}
