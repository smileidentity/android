package com.smileidentity.unico

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UnicoProcessViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UnicoProcessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UnicoProcessViewModel(UnicoProcessManager(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
