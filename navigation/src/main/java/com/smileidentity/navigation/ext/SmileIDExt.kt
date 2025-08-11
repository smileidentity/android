package com.smileidentity.navigation.ext

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions

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
