package net.arwix.placeselectorfeature.parts.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import net.arwix.placeselector.common.mvi.SimpleViewModel
import net.arwix.placeselector.parts.list.ui.PlaceListContract
import net.arwix.placeselector.parts.list.ui.PlaceListViewModel
import net.arwix.placeselectorfeature.R
import net.arwix.placeselectorfeature.ui.PlaceWizardBottomBarComponent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListScreen(
    viewModel: PlaceListViewModel,
    onNavigateUp: () -> Unit,
    onNextScreen: () -> Unit
) {

    LaunchedEffect(SimpleViewModel.SIDE_EFFECT_LAUNCH_ID) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                PlaceListContract.Effect.ToEdit -> onNextScreen()
            }
        }.collect()
    }

    val scrollBehavior: TopAppBarScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(
            rememberTopAppBarState()
        )

    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.place_screen_title_list)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                PlaceWizardBottomBarComponent(
                    isEnableNextStep = true,
                    isShowNextStep = true,
                    previousName = stringResource(R.string.place_navigation_bottom_bar_back),
                    nextName = stringResource(R.string.place_navigation_bottom_bar_add),
                    onPreviousClick = onNavigateUp,
                    onNextClick = {
                        viewModel.doEvent(PlaceListContract.Event.AddPlace)
                    }
                )
            }
        ) { paddingValues ->

            val direction = LocalLayoutDirection.current

            val paddingExceptTopBar by remember(paddingValues, direction) {
                derivedStateOf {
                    PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateStartPadding(direction),
                        end = paddingValues.calculateEndPadding(direction),
                        bottom = 0.dp
                    )
                }
            }

            val paddingExceptBottomBar by remember(paddingValues) {
                derivedStateOf {
                    PaddingValues(
                        top = 8.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp
                    )
                }
            }

            val state by viewModel.state.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingExceptTopBar)
            ) {
                PlaceListSection(
                    modifier = Modifier,
                    state = state,
                    eventHandler = viewModel,
                    snackbarHostState = snackbarHostState,
                    listContentPadding = paddingExceptBottomBar,
                )
//                BottomNavigationSpacer()
            }
        }
    }
}