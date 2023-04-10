package net.arwix.placeselector.parts.edit.timezone.ui

import net.arwix.placeselector.common.getGmtOffsetText
import net.arwix.placeselector.common.getLongName
import net.arwix.placeselector.common.getName
import net.arwix.placeselector.common.mvi.UIEvent
import net.arwix.placeselector.common.mvi.UISideEffect
import net.arwix.placeselector.common.mvi.UIState
import net.arwix.placeselector.data.inner.InnerEditData
import net.arwix.placeselector.data.timezone.TimeZoneDisplayEntry
import java.time.Instant
import java.time.ZoneId

class PlaceEditTimeZoneContract {

    data class State(
        val listZones: List<TimeZoneDisplayEntry> = listOf(),
        val autoTimeZoneEntry: AutoTimeZoneEntry? = null,
        val selectedItem: SelectedItem? = null,
        val finishStepAvailable: Boolean = false
    ) : UIState {

        sealed class SelectedItem {
            data class FromList(val value: TimeZoneDisplayEntry) : SelectedItem()
            data class FromAutoTimeZone(val value: AutoTimeZoneEntry.Ok) : SelectedItem()

            companion object {
                fun createSelectedItem(
                    data: InnerEditData,
                    instant: Instant
                ): SelectedItem? {
                    data.zoneId ?: return null
                    return if (data.isAutoZone) {
                        FromAutoTimeZone(
                            AutoTimeZoneEntry.Ok(
                                latitude = data.latitude,
                                longitude = data.longitude,
                                timeZoneDisplayEntry = getDisplayEntry(data.zoneId, instant)
                            )
                        )
                    } else {
                        FromList(
                            TimeZoneDisplayEntry(data.zoneId, instant)
                        )
                    }
                }
            }
        }

        sealed class AutoTimeZoneEntry {
            object Denied : AutoTimeZoneEntry()
            data class Loading(
                val latitude: Double,
                val longitude: Double
            ) : AutoTimeZoneEntry()

            data class Ok(
                val latitude: Double,
                val longitude: Double,
                val timeZoneDisplayEntry: TimeZoneDisplayEntry
            ) :
                AutoTimeZoneEntry()

            data class Error(
                val latitude: Double,
                val longitude: Double,
                val error: Throwable
            ) : AutoTimeZoneEntry()

            fun getLatitudeLongitude(): Pair<Double, Double>? {
                return when (this) {
                    is Error -> latitude to longitude
                    Denied -> null
                    is Loading -> latitude to longitude
                    is Ok -> latitude to longitude
                }
            }
        }

        companion object {
            fun TimeZoneDisplayEntry.isSelectedItem(item: SelectedItem?) =
                if (item is SelectedItem.FromList) item.value == this
                else false

            fun AutoTimeZoneEntry.isSelectedItem(item: SelectedItem?) =
                if (item is SelectedItem.FromAutoTimeZone) item.value == this
                else false
        }
    }

    sealed class Event : UIEvent {
        object GetPremium : Event()
        data class SelectItem(val item: State.SelectedItem) : Event()
        object Submit : Event()
        object ClearData : Event()
    }

    sealed class Effect : UISideEffect {
        object OnSubmitData : Effect()
    }

}

fun getDisplayEntry(zoneId: ZoneId, now: Instant): TimeZoneDisplayEntry {
    val offset = zoneId.rules.getOffset(now)
    val isLight = zoneId.rules.isDaylightSavings(now)
    val name = zoneId.getName()
    val longName = getLongName(zoneId, now, isLight)
    return TimeZoneDisplayEntry(
        zoneId,
        offset,
        name,
        longName,
        zoneId.getGmtOffsetText(now)
    )
}