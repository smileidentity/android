package com.smileidentity.viewmodel

import com.smileidentity.models.Document
import com.smileidentity.randomJobId
import com.smileidentity.randomUserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentViewModelTest {
    private lateinit var subject: DocumentViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        subject = DocumentViewModel(randomUserId(), randomJobId(), Document("KE", "ID_CARD"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState should be initialized with the correct defaults`() {
        val uiState = subject.uiState.value
        assertEquals(null, uiState.frontDocumentImageToConfirm)
        assertEquals(null, uiState.errorMessage)
    }
}
