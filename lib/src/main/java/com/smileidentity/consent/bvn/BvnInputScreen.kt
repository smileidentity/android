package com.smileidentity.consent.bvn

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smileidentity.compose.components.BottomPinnedColumn
import com.smileidentity.viewmodel.BvnConsentViewModel
import com.smileidentity.viewmodel.viewModelFactory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BvnInputScreen(
    modifier: Modifier = Modifier,
    viewModel: BvnConsentViewModel = viewModel(
        factory = viewModelFactory {
            BvnConsentViewModel()
        },
    ),
) {
    BottomPinnedColumn(
        scrollableContent = {
        },
        pinnedContent = {
        },
        columnWidth = 320.dp,
    )
}
