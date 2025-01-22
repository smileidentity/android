package com.smileidentity.sample.repo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.smileidentity.models.Config
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataStoreRepositoryTest {
    @Before
    fun setUp() {
        runBlocking {
            DataStoreRepository.clear()
        }
    }

    @Test
    fun test_getConfig_and_setConfig() = runTest {
        // given
        val expected = Config(
            partnerId = "partnerId",
            authToken = "authToken",
            prodLambdaUrl = "prodBaseUrl",
            testLambdaUrl = "sandboxBaseUrl",
        )

        // when
        DataStoreRepository.setConfig(expected)

        // then
        val actual = DataStoreRepository.getConfig().first()
        assertEquals(expected, actual)
    }

    @Test
    fun testClearConfig() = runTest {
        // given
        val expected = Config(
            partnerId = "partnerId",
            authToken = "authToken",
            prodLambdaUrl = "prodBaseUrl",
            testLambdaUrl = "sandboxBaseUrl",
        )
        DataStoreRepository.setConfig(expected)

        // when
        DataStoreRepository.clearConfig()

        // then
        val actual = DataStoreRepository.getConfigJsonString().first()
        assertNull(actual)
    }
}
