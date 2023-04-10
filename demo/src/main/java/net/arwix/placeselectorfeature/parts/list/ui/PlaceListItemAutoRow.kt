package net.arwix.placeselectorfeature.parts.list.ui

import android.Manifest
import android.app.Activity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import net.arwix.placeselector.common.except
import net.arwix.placeselector.common.getGmtOffsetText
import net.arwix.placeselector.data.room.PlaceData
import net.arwix.placeselectorfeature.R
import net.arwix.placeselectorfeature.common.await
import java.time.ZoneId

@Composable
internal fun AutoAllowRow(
    modifier: Modifier = Modifier,
    data: PlaceData,
    onUpdateLocation: () -> Unit
) {
    AutoAllowRow(
        modifier, data.name, data.subName, data.latitude, data.longitude, data.zone, onUpdateLocation
    )
}

@Composable
internal fun AutoAllowRow(
    modifier: Modifier = Modifier,
    title: String?,
    subTitle: String?,
    latitude: Double,
    longitude: Double,
    zoneId: ZoneId,
    onUpdateLocation: () -> Unit
) {
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == Activity.RESULT_OK) onUpdateLocation()
        }
    )

    AutoContainer(
        modifier = modifier
    ) {
        BodyPart(
            title = title ?: "",
            subTitle = subTitle ?: "",
            latitude = latitude,
            longitude = longitude,
            zoneId = zoneId
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                text = zoneId.getGmtOffsetText()
            )
            OutlinedButton(
                modifier = Modifier.semantics { Role.Button },
                onClick = createOnUpdateLocationClick(
                    requestPermissionLauncher,
                    onUpdateLocation
                )
            ) {
                Text(
                    text = stringResource(R.string.place_location_update).toUpperCase(Locale.current)
                )
            }
        }
    }
}

@Composable
internal fun AutoAllowButNotDataRow(
    modifier: Modifier = Modifier,
    onAutoUpdate: () -> Unit
) {
    AutoContainer(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier
                    .semantics { Role.Button },
                onClick = { onAutoUpdate() }) {
                Text(
                    text = stringResource(R.string.place_location_get_location)
                        .toUpperCase(Locale.current)
                )
            }
        }
    }
}

@Composable
internal fun AutoDeniedRow(
    modifier: Modifier = Modifier,
    onLocationPermission: (isGrained: Boolean) -> Unit
) {
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            onLocationPermission(
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            )
        }
    )

    AutoContainer(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.place_location_permission_info),
        )
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            })
        {
            Text(
                text = stringResource(R.string.place_location_permission_button).toUpperCase(
                    Locale.current
                )
            )
        }
    }
}

@Composable
private fun AutoContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    PlaceListItemContainer(modifier = modifier) {
        Text(
            text = stringResource(R.string.place_location_header_auto).uppercase(),
            style = MaterialTheme.typography.labelLarge
        )
        content()
    }
}

@Composable
private fun createOnUpdateLocationClick(
    launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    onLocationUpdateClick: () -> Unit
): () -> Unit {
    val scope = rememberCoroutineScope()
    val client = LocationServices.getSettingsClient(LocalContext.current)
    val request =
        LocationRequest.create().apply { priority = Priority.PRIORITY_HIGH_ACCURACY }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
    return {
        scope.launch {
            runCatching { client.checkLocationSettings(builder.build()).await() }
                .except<CancellationException, LocationSettingsResponse?>()
                .onFailure {
                    if (it is ApiException && it.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                        it as ResolvableApiException
                        // https://stackoverflow.com/questions/31235564/locationsettingsrequest-dialog-to-enable-gps-onactivityresult-skipped/39579124#39579124
                        val intentSenderRequest =
                            IntentSenderRequest.Builder(it.resolution.intentSender).build()
                        launcher.launch(intentSenderRequest)
                    } else {
                        onLocationUpdateClick()
                    }
                }
                .onSuccess {
                    onLocationUpdateClick()
                }
        }
    }
}