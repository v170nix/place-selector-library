package net.arwix.placeselectorfeature.parts.list.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import net.arwix.placeselector.parts.list.data.PlaceListItemUI

@Composable
internal fun PlaceListItemRow(
    modifier: Modifier = Modifier,
    item: PlaceListItemUI,
    onLocationPermission: (isGrained: Boolean) -> Unit,
    onAutoUpdate: () -> Unit,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onSelect: (PlaceListItemUI) -> Unit,
    onEdit: (PlaceListItemUI) -> Unit,
    onDelete: (PlaceListItemUI) -> Unit
) {

    val backgroundColor by animateColorAsState(
        if (item.isSelected) selectedContainerColor else MaterialTheme.colorScheme.surfaceVariant
    )

    val textColor by animateColorAsState(
        if (item.isSelected) selectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant
    )

    val innerModifier = Modifier
        .selectable(
            enabled = item.isSelectable,
            selected = item.isSelected,
            onClick = {
                onSelect(item)
            },
            role = Role.Button
        )
        .padding(16.dp)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = textColor
        )
    ) {
        when (item) {
            is PlaceListItemUI.Auto -> {
                when (val state = item.state) {
                    is PlaceListItemUI.Auto.State.Allow -> {
                        val data = state.data
                        if (data != null) {
                            AutoAllowRow(innerModifier, data, onAutoUpdate)
                        } else {
                            AutoAllowButNotDataRow(innerModifier, onAutoUpdate)
                        }
                    }
                    PlaceListItemUI.Auto.State.Denied -> AutoDeniedRow(
                        innerModifier,
                        onLocationPermission
                    )
                    PlaceListItemUI.Auto.State.DeniedRationale -> AutoDeniedRow(
                        innerModifier,
                        onLocationPermission
                    )
                    PlaceListItemUI.Auto.State.None -> AutoDeniedRow(
                        innerModifier,
                        onLocationPermission
                    )
                }
            }
            is PlaceListItemUI.Custom -> PlaceListCustomItemRow(
                modifier = innerModifier,
                data = item.place,
                onEditItem = {
                    onEdit(item)
                },
                onDeleteItem = {
                    onDelete(item)
                }
            )
        }
    }
}