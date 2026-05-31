package com.k.image.cropview.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.k.image.cropview.examples.exampleone.ExampleOneScreen
import com.k.image.cropview.examples.exampletwo.ExampleTwoScreen
import com.k.image.cropview.ui.home.HomeScreen

/**
 * Top-level navigation graph for the sample app: a Home list plus one route per example.
 */
@Composable
fun SampleNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = SampleRoutes.HOME) {
        composable(SampleRoutes.HOME) {
            HomeScreen(
                examples = SampleExample.entries,
                onExampleClick = { example -> navController.navigate(example.route) },
            )
        }

        composable(SampleExample.EXAMPLE_ONE.route) {
            ExampleOneScreen(onBack = { navController.popBackStack() })
        }

        composable(SampleExample.EXAMPLE_TWO.route) {
            ExampleTwoScreen(onBack = { navController.popBackStack() })
        }
    }
}