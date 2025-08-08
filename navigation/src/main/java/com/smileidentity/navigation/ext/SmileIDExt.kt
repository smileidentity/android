package com.smileidentity.navigation.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.smileidentity.navigation.graph.SmileIDGraph

// todo demo for now, will need to remove this
@Composable
fun SmartSelfieEnrollment(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    DestinationsNavHost(
        navController = navController,
        navGraph = SmileIDGraph,
        defaultTransitions = DefaultFadingTransitions,
    )
}
