package com.smileidentity.sample.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smileidentity.SmileID
import com.smileidentity.sample.BuildConfig
import com.smileidentity.sample.ProductScreen
import com.smileidentity.sample.R
import com.smileidentity.sample.Screen

@Composable
fun ProductSelectionScreen(
    modifier: Modifier = Modifier,
    onProductSelected: (Screen) -> Unit,
) {
    val density = LocalDensity.current
    var desiredItemMinHeight by remember { mutableStateOf(0.dp) }
    val products = listOf(
        ProductScreen.SmartSelfieEnrollment,
        ProductScreen.SmartSelfieAuthentication,
        ProductScreen.EnhancedKyc,
        ProductScreen.BiometricKyc,
        ProductScreen.DocumentVerification,
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f),
        ) {
            Text(
                stringResource(R.string.test_our_products),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                items(products) {
                    FilledTonalButton(
                        onClick = { onProductSelected(it) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier
                                .onPlaced {
                                    // Ensures all the buttons in the grid are the same height while
                                    // still allowing the text to wrap to new lines as needed
                                    with(density) {
                                        if (desiredItemMinHeight < it.size.height.toDp()) {
                                            desiredItemMinHeight = it.size.height.toDp()
                                        }
                                    }
                                }
                                .defaultMinSize(minHeight = desiredItemMinHeight),
                        ) {
                            Image(
                                painterResource(it.icon),
                                contentDescription = stringResource(
                                    R.string.product_name_icon,
                                    stringResource(it.label),
                                ),
                                modifier = Modifier.size(64.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(it.label), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
        SelectionContainer {
            Text(
                text = stringResource(
                    R.string.version_info,
                    SmileID.config.partnerId,
                    BuildConfig.VERSION_NAME,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.typography.labelMedium.color.copy(alpha = .5f),
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

@Preview
@Composable
private fun ProductSelectionScreenPreview() {
    SmileID.initialize(LocalContext.current, enableCrashReporting = false)
    SmileIDTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProductSelectionScreen { }
        }
    }
}
