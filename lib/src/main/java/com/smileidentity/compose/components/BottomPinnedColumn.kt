package com.smileidentity.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smileidentity.R
import com.smileidentity.compose.preview.Preview
import com.smileidentity.compose.preview.SmilePreviews

@Composable
internal fun BottomPinnedColumn(
    scrollableContent: @Composable () -> Unit,
    pinnedContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    columnWidth: Dp = 0.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxHeight()
                .then(
                    if (columnWidth != 0.dp) {
                        Modifier.width(columnWidth)
                    } else {
                        Modifier.fillMaxWidth()
                    },
                )
                .verticalScroll(rememberScrollState())
                .weight(1f),
        ) {
            scrollableContent()
        }
        Column(
            horizontalAlignment = horizontalAlignment,
            modifier = Modifier
                .then(
                    if (columnWidth != 0.dp) {
                        Modifier.width(columnWidth)
                    } else {
                        Modifier.fillMaxWidth()
                    },
                )
                .padding(8.dp),
        ) {
            pinnedContent()
        }
    }
}

@SmilePreviews
@Composable
private fun BottomPinnedColumnPreview() {
    Preview {
        BottomPinnedColumn(
            scrollableContent = { Text(text = stringResource(id = R.string.si_allow)) },
            pinnedContent = { Text(text = stringResource(id = R.string.si_cancel)) },
        )
    }
}
