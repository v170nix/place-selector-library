package net.arwix.placeselectorfeature.parts.edit.location.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import net.arwix.placeselector.common.mvi.EventHandler
import net.arwix.placeselector.data.PlaceAutocompleteResult
import net.arwix.placeselector.parts.edit.location.ui.PlaceEditLocationContract
import net.arwix.placeselectorfeature.R
import net.arwix.placeselectorfeature.parts.edit.location.ui.data.InputTextFieldState
import net.arwix.placeselectorfeature.ui.InputTextField
import kotlin.math.abs

private const val BACK_HANDLER_SEARCH_TIMEOUT = 300L

@Suppress("MagicNumber")
@Composable
fun InputLocationBoxPart(
    modifier: Modifier = Modifier,
    state: PlaceEditLocationContract.State.InputState,
    eventHandler: EventHandler<PlaceEditLocationContract.Event>,
    onPreviousClick: () -> Unit
) {
    val latTextError =
        stringResource(R.string.place_location_error_range, -90, 90)
    val lngTextError =
        stringResource(R.string.place_location_error_range, -180, 180)

    val latState by remember(state.latitude, eventHandler) {
        derivedStateOf {
            InputTextFieldState(
                state.latitude,
                onValueChange = {
                    eventHandler.doEvent(
                        PlaceEditLocationContract.Event.ChangeLatitudeFromInput(
                            it
                        )
                    )
                },
                isError = isLatitudeError(state.latitude),
                textError = latTextError
            )
        }
    }

    val lngState by remember(state.longitude, eventHandler) {
        derivedStateOf {
            InputTextFieldState(
                state.longitude,
                onValueChange = {
                    eventHandler.doEvent(
                        PlaceEditLocationContract.Event.ChangeLongitudeFromInput(
                            it
                        )
                    )
                },
                isError = isLongitudeError(state.longitude),
                textError = lngTextError
            )
        }
    }

    var searchResultTime by remember(Unit) { mutableStateOf(0L) }

    BackHandler {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime > searchResultTime + BACK_HANDLER_SEARCH_TIMEOUT) {
            onPreviousClick()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        InputLocationBoxPart(
            Modifier.padding(start = 16.dp, top = 0.dp, end = 16.dp),
            state.name,
            state.subName,
            latState,
            lngState,
            onSearchClick = {
            },
            onSearchResult = { placeResult: PlaceAutocompleteResult ->
                searchResultTime = SystemClock.elapsedRealtime()
                eventHandler.doEvent(
                    PlaceEditLocationContract.Event.ChangeLocationFromPlace(
                        placeResult
                    )
                )
            }
        )
    }
}

private fun isLatitudeError(latitude: String): Boolean {
    if (latitude.isBlank()) return false
    val dLatitude = latitude.toDoubleOrNull() ?: return true
    return abs(dLatitude) > 90
}

private fun isLongitudeError(longitude: String): Boolean {
    if (longitude.isBlank()) return false
    val dLongitude = longitude.toDoubleOrNull() ?: return true
    return abs(dLongitude) > 180
}


@Composable
@Suppress("MagicNumber")
private fun InputLocationBoxPart(
    modifier: Modifier = Modifier,
    title: String?,
    subTitle: String?,
    latitudeFieldState: InputTextFieldState,
    longitudeFieldState: InputTextFieldState,
    onSearchClick: () -> Unit,
    onSearchResult: (PlaceAutocompleteResult) -> Unit,
) {
    var expandableInput by remember(Unit) { mutableStateOf(false) }
    var isShowCard by remember(Unit) { mutableStateOf(true) }
    DisposableEffect(Unit) {
        onDispose {
            isShowCard = true
        }
    }

    BackHandler(expandableInput) {
        expandableInput = false
    }

    // https://developer.android.com/codelabs/jetpack-compose-animation?hl=en&continue=https%3A%2F%2Fcodelabs.developers.google.com%2F%3Fcat%3Dandroid#3

    Card(modifier.alpha(if (isShowCard) .9f else 0f)) {
        Column(modifier = Modifier.padding(8.dp)) {
            InputTitlePart(
                title = title ?: subTitle ?: "",
                isExpanded = expandableInput,
                onSearchClick = { isShowCard = false; onSearchClick(); },
                onSearchResult = { isShowCard = true; onSearchResult(it) },
                onExpandClick = { expandableInput = !expandableInput }
            )
            AnimatedVisibility(visible = expandableInput) {
                InputTextFieldsPart(
                    latitudeFieldState,
                    longitudeFieldState
                ) {
                    expandableInput = false
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun InputTitlePart(
    title: String,
    isExpanded: Boolean = false,
    onSearchClick: () -> Unit,
    onSearchResult: (PlaceAutocompleteResult) -> Unit,
    onExpandClick: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

//    val placeFields = listOf(
//        Place.Field.ID,
//        Place.Field.NAME,
//        Place.Field.LAT_LNG,
//        Place.Field.ADDRESS_COMPONENTS,
//        Place.Field.ADDRESS
//    )

    val requestAutocompleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { activityResult ->
            keyboardController?.hide()
            focusManager.clearFocus()
            val data = activityResult.data ?: return@rememberLauncherForActivityResult
//            val result = when (activityResult.resultCode) {
//                Activity.RESULT_OK -> PlaceAutocompleteResult.Ok(
//                    Autocomplete.getPlaceFromIntent(data)
//                )
//                AutocompleteActivity.RESULT_ERROR -> PlaceAutocompleteResult.Error(
//                    Autocomplete.getStatusFromIntent(data)
//                )
//                Activity.RESULT_CANCELED -> PlaceAutocompleteResult.Canceled
//                else -> return@rememberLauncherForActivityResult
//            }
//            onSearchResult(result)
        }
    )

    val context = LocalContext.current

//    val onPlaceSearchClick by rememberUpdatedState {
//        if (Places.isInitialized()) {
//            requestAutocompleteLauncher.launch(
//                Autocomplete
//                    .IntentBuilder(AutocompleteActivityMode.OVERLAY, placeFields)
//                    .build(context)
//            )
//            onSearchClick()
//        }
//    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        PlaceLocationSearch(onClick = onPlaceSearchClick)
        Text(
            modifier = Modifier
                .semantics { role = Role.Button }
                .weight(1f)
//                .clickable(onClick = onPlaceSearchClick)
//                .padding(horizontal = Dimens.grid_1)
            ,
            maxLines = 1,
            style = MaterialTheme.typography.titleLarge,
            text = title
        )
        IconButton(
            modifier = Modifier.semantics { role = Role.Button },
            onClick = onExpandClick
        ) {
            if (isExpanded) {
                Icon(Icons.Filled.KeyboardArrowUp, null)
            } else {
                Icon(Icons.Filled.KeyboardArrowDown, null)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun InputTextFieldsPart(
    latitudeFieldState: InputTextFieldState,
    longitudeFieldState: InputTextFieldState,
    onDoneClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val latitudeFocusRequester = remember { FocusRequester() }
    val longitudeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        latitudeFocusRequester.requestFocus()
    }

    Column(Modifier.padding(8.dp)) {
        InputTextField(
            modifier = Modifier
                .focusRequester(latitudeFocusRequester)
                .focusProperties { next },
            labelId = R.string.place_location_latitude,
            textFieldState = latitudeFieldState,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions {
                longitudeFocusRequester.requestFocus()
            },
        )
        Spacer(Modifier.height(8.dp))

        InputTextField(
            modifier = Modifier
                .focusRequester(longitudeFocusRequester)
                .focusProperties { down },
            labelId = R.string.place_location_longitude,
            textFieldState = longitudeFieldState,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onDoneClick()
                }
            )
        )
    }
}
