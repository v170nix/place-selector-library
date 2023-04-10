package net.arwix.placeselectorfeature.parts.list.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.arwix.placeselector.common.mvi.EventHandler
import net.arwix.placeselector.parts.list.data.PlaceListItemUI
import net.arwix.placeselector.parts.list.ui.PlaceListContract

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaceListComponent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    state: PlaceListContract.State,
    eventHandler: EventHandler<PlaceListContract.Event>,
    onLocationPermission: (isGrained: Boolean) -> Unit,
    onLocationUpdate: () -> Unit,
    onShowUndoSnackbar: (PlaceListItemUI.Custom) -> Unit
) {

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
//        state = rememberPlaceLazyListState()
    ) {
        items(
            items = state.list,
            key = { item -> item.getKey() }
        ) { item ->
            PlaceListItemRow(
                modifier = Modifier.animateItemPlacement(),
                item = item,
                onLocationPermission = onLocationPermission,
                onAutoUpdate = onLocationUpdate,
                onSelect = { place ->
                    eventHandler.doEvent(PlaceListContract.Event.SelectItem(place))
                },
                onEdit = {
                    eventHandler.doEvent(PlaceListContract.Event.EditItem(it as PlaceListItemUI.Custom))
                },
                onDelete = {
                    eventHandler.doEvent(PlaceListContract.Event.DeleteItem(it as PlaceListItemUI.Custom))
                    onShowUndoSnackbar(it)
                }
            )
        }
    }
}