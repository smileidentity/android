package com.smileidentity.sample.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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

private val products = ProductScreen.entries
private val roundedCornerShape = RoundedCornerShape(16.dp)

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
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 2.dp)
                .weight(1f),
        ) {
            Text(
                stringResource(R.string.test_our_products),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = spacedBy(8.dp),
                horizontalArrangement = spacedBy(8.dp),
                modifier = Modifier.clip(roundedCornerShape),
            ) {
                items(products) {
                    ProductCell(
                        productScreen = it,
                        onProductSelected = onProductSelected,
                        modifier = Modifier.defaultMinSize(minHeight = 164.dp),
                    )
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
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
    }
}

@Composable
private fun ProductCell(
    productScreen: ProductScreen,
    onProductSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = { onProductSelected(productScreen) },
        shape = roundedCornerShape,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
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
