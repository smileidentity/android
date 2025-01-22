package com.smileidentity.compose.biometric

import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility

@NavHostGraph(
    route = "biometric_route",
    visibility = CodeGenVisibility.INTERNAL,
)
internal annotation class BiometricGraph
