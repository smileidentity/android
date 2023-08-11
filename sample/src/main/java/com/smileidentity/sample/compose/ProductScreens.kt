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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smileidentity.SmileID
import com.smileidentity.sample.BuildConfig
import com.smileidentity.sample.ProductScreen
import com.smileidentity.sample.R
import com.smileidentity.sample.Screen

val products = listOf(
    ProductScreen.SmartSelfieEnrollment,
    ProductScreen.SmartSelfieAuthentication,
    ProductScreen.EnhancedKyc,
    ProductScreen.BiometricKyc,
    ProductScreen.DocumentVerification,
)

@Composable
fun ProductSelectionScreen(
    onProductSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .weight(1f),
        ) {
            Text(
                stringResource(R.string.test_our_products),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            ProductsGrid(onProductSelected)
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

/**
 * We'd like each product cell to be the same height. However, we don't know the height of the
 * tallest cell until we've measured them all. So we need to measure them all twice. Once to find
 * the tallest cell, and again to actually lay them out.
 *
 * We do this by using a [SubcomposeLayout] with 2 subcompositions. The first one measures each cell
 * and returns the tallest height. The second subcomposition creates the actual grid layout.
 */
@Composable
private fun ProductsGrid(onProductSelected: (Screen) -> Unit) {
    SubcomposeLayout { constraints ->
        // The true/false passed to subcompose is merely because we need 2 unique keys
        val maxHeight = subcompose(true) {
            products.map { ProductCell(it, onProductSelected, 0.dp) }
        }.maxOf { it.measure(constraints).measuredHeight.toDp() }

        val contentPlaceable = subcompose(false) {
            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                items(products) {
                    ProductCell(it, onProductSelected, maxHeight)
                }
            }
        }.first().measure(constraints)

        layout(contentPlaceable.measuredWidth, maxHeight.roundToPx()) {
            contentPlaceable.place(0, 0)
        }
    }
}

/**
 * [minHeight] is used so that 0.dp can be passed to the [ProductCell] composable as part of an
 * initial pass to measure the final max height. Once a max height is determined, the minHeight
 * is guaranteed to be greater than 0.dp
 */
@Composable
private fun ProductCell(
    productScreen: ProductScreen,
    onProductSelected: (Screen) -> Unit,
    minHeight: Dp,
) {
    FilledTonalButton(
        onClick = { onProductSelected(productScreen) },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.defaultMinSize(minHeight = minHeight),
        ) {
            Image(
                painterResource(productScreen.icon),
                contentDescription = stringResource(
                    R.string.product_name_icon,
                    stringResource(productScreen.label),
                ),
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(productScreen.label), textAlign = TextAlign.Center)
        }
    }
}

@Preview
@Composable
private fun ProductSelectionScreenPreview() {
    SmileID.initialize(LocalContext.current, enableCrashReporting = false)
    SmileIDTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProductSelectionScreen(onProductSelected = {})
        }
    }
}
