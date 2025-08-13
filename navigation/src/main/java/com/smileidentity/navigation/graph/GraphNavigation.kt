package com.smileidentity.navigation.graph

import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import com.ramcosta.composedestinations.generated.navigation.destinations.OrchestratedInstructionsScreenDestination
import com.ramcosta.composedestinations.generated.navigation.navgraphs.RootNavGraph
import com.smileidentity.navigation.navigation.SmileIDNavigationHost

@Composable
fun GraphNavigation(modifier: Modifier = Modifier) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController()
    navController.navigatorProvider += bottomSheetNavigator

    SmileIDNavigationHost(
        modifier = modifier,
        startDestination = OrchestratedInstructionsScreenDestination,
        destinations = RootNavGraph,
    )
}
