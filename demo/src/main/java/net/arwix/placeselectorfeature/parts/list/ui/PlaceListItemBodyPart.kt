package net.arwix.placeselectorfeature.parts.list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.arwix.placeselector.common.getLongName
import net.arwix.placeselector.common.getName
import net.arwix.placeselector.common.latToString
import net.arwix.placeselector.common.lngToString
import net.arwix.placeselectorfeature.R
import java.time.Instant
import java.time.ZoneId

@Composable
internal fun BodyPart(
    modifier: Modifier = Modifier,
    title: String?,
    subTitle: String?,
    latitude: Double,
    longitude: Double,
    zoneId: ZoneId
) {
    Column(
        modifier = modifier,
    ) {
        LocationPart(latitude = latitude, longitude = longitude)
        TitlePart(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            title = title ?: "",
            subTitle = subTitle ?: ""
        )
        TimeZonePart(zoneId = zoneId)
    }
}

@Composable
private fun LocationPart(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double
) {
    Row(modifier) {
        Text(
            style = MaterialTheme.typography.labelMedium,
            text = latToString(
                latitude,
                stringResource(R.string.place_location_north),
                stringResource(R.string.place_location_south)
            )
        )
        Text(
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 8.dp),
            text = lngToString(
                longitude,
                stringResource(R.string.place_location_east),
                stringResource(R.string.place_location_west)
            )
        )
    }
}

@Composable
private fun TitlePart(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    title: String?,
    subTitle: String?
) {
    if (title.isNullOrBlank() && subTitle.isNullOrBlank()) return
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        if (!title.isNullOrBlank() || !subTitle.isNullOrBlank())
            Text(
                style = MaterialTheme.typography.headlineMedium,
                text = title ?: subTitle!!
            )
        if (!subTitle.isNullOrBlank() && !title.isNullOrBlank())
            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = subTitle
            )
    }
}

@Composable
private fun TimeZonePart(
    modifier: Modifier = Modifier,
    zoneId: ZoneId
) {
    val longName = zoneId.getLongName(Instant.now()).ifEmpty { zoneId.getName() }
    Column(modifier) {
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = longName
        )
    }
}