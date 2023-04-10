package net.arwix.placeselectorfeature.parts.list.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.arwix.placeselector.common.mvi.EventHandler
import net.arwix.placeselector.parts.list.ui.PlaceListContract
import net.arwix.placeselectorfeature.R

@Composable
internal fun PlaceListSection(
    modifier: Modifier = Modifier,
    state: PlaceListContract.State,
    eventHandler: EventHandler<PlaceListContract.Event>,
    listContentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
) {
//    val state by model.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {

        val undoMessage = stringResource(R.string.place_location_undo_delete_message)
        val undoString = stringResource(R.string.place_location_undo_delete_button).uppercase()

        PlaceListComponent(
            Modifier.fillMaxSize(),
            contentPadding = listContentPadding,
            state = state,
            eventHandler = eventHandler,
            onLocationPermission = { isGrained ->
                eventHandler.doEvent(PlaceListContract.Event.UpdateLocationPermission(isGrained))
            },
            onLocationUpdate = {
                eventHandler.doEvent(PlaceListContract.Event.UpdateLocation)
            },
            onShowUndoSnackbar = { item ->
                coroutineScope.launch {
                    val snackbarResult = snackbarHostState.showSnackbar(
                        message = undoMessage,
                        actionLabel = undoString
                    )
                    when (snackbarResult) {
                        SnackbarResult.Dismissed -> {
                        }
                        SnackbarResult.ActionPerformed -> {
                            eventHandler.doEvent(PlaceListContract.Event.UndoDeleteItem(item))
                        }
                    }
                }
            }
        )
    }
}