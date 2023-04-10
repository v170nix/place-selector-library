package net.arwix.placeselector.common

import android.icu.text.TimeZoneNames
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

fun ZoneId.getGmtOffsetText(
    instant: Instant = Instant.now()
) = buildString {
    append("GMT")
    append(this@getGmtOffsetText.rules.getOffset(instant).id.takeIf { it != "Z" } ?: "+00:00")
}

fun ZoneId.getName(): String {
    val name = getName(TimeZoneNames.getInstance(Locale.getDefault()), this)
        ?: getDefaultExemplarLocationName(this.id)
    return name ?: ""
}

fun ZoneId.getLongName(now: Instant): String {
    val f = DateTimeFormatter.ofPattern("zzzz", Locale.getDefault())
    return ZonedDateTime.ofInstant(now, this).format(f)
}

fun getLongName(
    names: TimeZoneNames,
    zoneId: ZoneId,
    now: Instant,
    isLight: Boolean
): String? {
    val nameType =
        if (isLight) TimeZoneNames.NameType.LONG_DAYLIGHT else TimeZoneNames.NameType.LONG_STANDARD
    return names.getDisplayName(zoneId.id, nameType, Date.from(now).time)
        ?: names.getDisplayName(
            getCanonicalID(zoneId),
            nameType,
            Date.from(now).time
        )
}

fun getLongName(
    zoneId: ZoneId,
    now: Instant,
    isLight: Boolean
): String {
    val name =
        getLongName(TimeZoneNames.getInstance(Locale.getDefault()), zoneId, now, isLight)
            ?: zoneId.getLongName(now)
    if (name.contains("GMT")) return getDefaultExemplarLocationName(zoneId.id) ?: ""
    return name
}

fun getName(names: TimeZoneNames, zoneId: ZoneId): String? =
    names.getExemplarLocationName(getCanonicalID(zoneId))

fun getCanonicalID(zoneId: ZoneId): String =
    android.icu.util.TimeZone.getCanonicalID(zoneId.id) ?: zoneId.id

private val LOC_EXCLUSION_PATTERN = Pattern.compile("SystemV/.*|.*/Riyadh8[7-9]")

/**
 * Default exemplar location name based on time zone ID.
 * For example, "America/New_York" -> "New York"
 * @param tzID the time zone ID
 * @return the exemplar location name or null if location is not available.
 */
private fun getDefaultExemplarLocationName(tzID: String): String? {
    if (tzID.isEmpty() || LOC_EXCLUSION_PATTERN.matcher(tzID).matches()) {
        return null
    }
    var location: String? = null
    val sep = tzID.lastIndexOf('/')
    if (sep > 0 && sep + 1 < tzID.length) {
        location = tzID.substring(sep + 1).replace('_', ' ')
    }
    return location
}
