package com.smileidentity.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smileidentity.SmileID
import com.smileidentity.sample.repo.DataStoreRepository
import kotlinx.coroutines.launch

class MainScreenViewModel : ViewModel() {
    fun clearJobs() {
        viewModelScope.launch {
            DataStoreRepository.clearJobs(SmileID.config.partnerId, !SmileID.useSandbox)
        }
    }
}
