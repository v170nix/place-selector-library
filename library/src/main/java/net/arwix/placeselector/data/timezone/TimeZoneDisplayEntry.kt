package net.arwix.placeselector.data.timezone

import net.arwix.placeselector.common.getGmtOffsetText
import net.arwix.placeselector.common.getLongName
import net.arwix.placeselector.common.getName
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

data class TimeZoneDisplayEntry constructor(
    val id: ZoneId,
    val zoneOffset: ZoneOffset,
    val displayName: String,
    val displayLongName: String,
    val gmtOffsetString: String
) {

    constructor(zoneId: ZoneId, instant: Instant) : this(
        zoneId,
        zoneId.rules.getOffset(instant),
        zoneId.getName(),
        zoneId.getLongName(instant),
        zoneId.getGmtOffsetText(instant)
    )


}