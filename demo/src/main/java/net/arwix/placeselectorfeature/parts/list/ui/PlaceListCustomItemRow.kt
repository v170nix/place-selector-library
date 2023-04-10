package net.arwix.placeselectorfeature.parts.list.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.arwix.placeselector.common.getGmtOffsetText
import net.arwix.placeselector.data.room.PlaceData

@Composable
internal fun PlaceListCustomItemRow(
    modifier: Modifier = Modifier,
    data: PlaceData,
    onEditItem: () -> Unit,
    onDeleteItem: () -> Unit
) {
    ItemContainer(
        modifier = modifier
    ) {
        BodyPart(
            title = data.name ?: "",
            subTitle = data.subName ?: "",
            latitude = data.latitude,
            longitude = data.longitude,
            zoneId = data.zone
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                text = data.zone.getGmtOffsetText()
            )
            IconButton(onClick = onEditItem) {
                Icon(Icons.Filled.Edit, "edit")
            }
            IconButton(onClick = onDeleteItem) {
                Icon(Icons.Filled.Delete, "delete")
            }
        }
    }
}

@Composable
internal fun ItemContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}