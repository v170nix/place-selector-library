package net.arwix.placeselectorfeature

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import net.arwix.placeselectorfeature.parts.edit.location.ui.DemoPlaceEditLocationViewModel
import net.arwix.placeselectorfeature.parts.edit.location.ui.PlaceEditLocationScreen
import net.arwix.placeselectorfeature.parts.list.ui.DemoListViewModel
import net.arwix.placeselectorfeature.parts.list.ui.PlaceListScreen
import net.arwix.placeselectorfeature.ui.theme.PlaceSelectorFeatureTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaceSelectorFeatureTheme {

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "list") {
                    composable("list") {
                        PlaceListScreen(
                            viewModel = hiltViewModel<DemoListViewModel>(
                                this@MainActivity
                            ),
                            onNavigateUp = {},
                            onNextScreen = {
                                navController.navigate("edit.location")
                            }
                        )
                    }
                    composable("edit.location") {
                        PlaceEditLocationScreen(
                            viewModel = hiltViewModel<DemoPlaceEditLocationViewModel>(
                                this@MainActivity
                            ),
                            onNavigateBackStack = {
                                navController.navigate("list")
                            },
                            onNextScreen = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PlaceSelectorFeatureTheme {
        Greeting("Android")
    }
}