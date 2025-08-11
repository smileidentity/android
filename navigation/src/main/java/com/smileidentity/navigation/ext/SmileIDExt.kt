package com.smileidentity.navigation.ext

import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign

// todo demo for now, will need to remove this
@Composable
fun SmartSelfieEnrollment(modifier: Modifier = Modifier) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController()
    navController.navigatorProvider += bottomSheetNavigator

//    ModalBottomSheetLayout(
//        bottomSheetNavigator = bottomSheetNavigator,
//        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
//    ) {
//        DestinationsNavHost(
//            navGraph = NavGraphs.root,
//            modifier = modifier
//                .fillMaxSize(),
//            navController = navController,
//            defaultTransitions = DefaultFadingTransitions,
//        )
//    }
}
