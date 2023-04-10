package net.arwix.placeselectorfeature.parts.edit.location.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import net.arwix.placeselector.parts.edit.location.ui.PlaceEditLocationContract
import net.arwix.placeselector.parts.edit.location.ui.PlaceEditLocationViewModel
import net.arwix.placeselectorfeature.R
import net.arwix.placeselectorfeature.ui.PlaceWizardBottomBarComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceEditLocationScreen(
    viewModel: PlaceEditLocationViewModel,
    onNavigateBackStack: () -> Unit,
    onNextScreen: () -> Unit
) {

    val innerNavigateBackStack by rememberUpdatedState {
        viewModel.doEvent(PlaceEditLocationContract.Event.ClearData)
        onNavigateBackStack()
    }

    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
//                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        stringResource(id = R.string.place_screen_title_select_location)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = innerNavigateBackStack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            PlaceWizardBottomBarComponent(
                isEnableNextStep = state.nextStepIsAvailable,
                isShowNextStep = true,
                previousName = stringResource(R.string.place_navigation_bottom_bar_back),
                nextName = stringResource(R.string.place_navigation_bottom_bar_next),
                onPreviousClick = innerNavigateBackStack,
                onNextClick = {
//                    viewModel.doEvent(PlaceEditLocationContract.Event.Submit)
                }
            )
        }
    ) { paddingValues ->

//        PlaceEditPositionSection(
//            modifier = Modifier.fillMaxSize(),
//            logoOffset = DpOffset(
//                Dimens.grid_2 + paddingValues.calculateLeftPadding(LocalLayoutDirection.current),
//                Dimens.grid_1 + paddingValues.calculateBottomPadding()
//            ),
//            eventHandler = viewModel,
//            state = state,
//            effectFlow = viewModel.effect,
//            onTimeZonePart = onNextScreen
//        )

        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            InputLocationBoxPart(
                state = state.inputState,
                eventHandler = viewModel,
                onPreviousClick = innerNavigateBackStack
            )
        }
    }
}