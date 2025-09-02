package com.smileidentity.attestation

import android.app.Activity
import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.smileidentity.security.arkana.ArkanaKeys
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class SmileIDStandardRequestIntegrityManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var smileIDIntegrityManager: SmileIDStandardRequestIntegrityManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock context
        context = mockk()

        // Mock static dependencies
        mockkStatic(IntegrityManagerFactory::class)
        mockkObject(ArkanaKeys.Global)

        // Setup default mocks
        every { ArkanaKeys.Global.gOOGLE_CLOUD_PROJECT_NUMBER } returns "123456789"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `warmUpTokenProvider should successfully prepare token provider`() = runTest {
        // Given
        val tokenProvider = FakeStandardIntegrityTokenProvider(
            Tasks.forResult(FakeStandardIntegrityToken()),
        )
        val factory = FakeSmileIDIntegrityManagerFactory(
            Tasks.forResult(tokenProvider),
        )
        setupIntegrityManager(factory)

        // When
        val result = smileIDIntegrityManager.warmUpTokenProvider()

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `warmUpTokenProvider should return success immediately if provider already initialized`() =
        runTest {
            // Given - First initialize the provider
            val tokenProvider = FakeStandardIntegrityTokenProvider(
                Tasks.forResult(FakeStandardIntegrityToken()),
            )
            val factory = FakeSmileIDIntegrityManagerFactory(
                Tasks.forResult(tokenProvider),
            )
            setupIntegrityManager(factory)

            val firstResult = smileIDIntegrityManager.warmUpTokenProvider()
            assertTrue(firstResult.isSuccess)

            // When - Call warmUp again
            val result = smileIDIntegrityManager.warmUpTokenProvider()

            // Then - Should return success without preparing again
            assertTrue("Result should be success", result.isSuccess)
            assertEquals(Unit, result.getOrNull())
        }

    @Test
    fun `warmUpTokenProvider should return failure when preparation fails`() = runTest {
        // Given
        val exception = Exception("Preparation failed")
        val factory = FakeSmileIDIntegrityManagerFactory(
            Tasks.forException(exception),
        )
        setupIntegrityManager(factory)

        // When
        val result = smileIDIntegrityManager.warmUpTokenProvider()

        // Then
        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Preparation failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `requestToken should successfully return token when provider is initialized`() = runTest {
        // Given
        val requestIdentifier = "test-request-id"
        val expectedToken = "integrity-token-123"
        val integrityToken = FakeStandardIntegrityToken(expectedToken)
        val tokenProvider = FakeStandardIntegrityTokenProvider(
            Tasks.forResult(integrityToken),
        )
        val factory = FakeSmileIDIntegrityManagerFactory(
            Tasks.forResult(tokenProvider),
        )
        setupIntegrityManager(factory)

        // First warm up the provider
        smileIDIntegrityManager.warmUpTokenProvider()

        // When
        val result = smileIDIntegrityManager.requestToken(requestIdentifier)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertEquals(expectedToken, result.getOrNull())
    }

    @Test
    fun `requestToken should fail when provider is not initialized`() = runTest {
        // Given
        val requestIdentifier = "test-request-id"
        val factory = FakeSmileIDIntegrityManagerFactory(
            Tasks.forResult(mockk<StandardIntegrityTokenProvider>()),
        )
        setupIntegrityManager(factory)

        // When - Request token without warming up
        val result = smileIDIntegrityManager.requestToken(requestIdentifier)

        // Then
        assertTrue("Result should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(
            "Exception should be IllegalStateException",
            exception is IllegalStateException,
        )
        assertEquals(
            "Integrity token provider is not initialized. Call warmUpTokenProvider() first.",
            exception?.message,
        )
    }

    @Test
    fun `requestToken should return failure when token request fails`() = runTest {
        // Given
        val requestIdentifier = "test-request-id"
        val requestException = Exception("Token request failed")
        val tokenProvider = FakeStandardIntegrityTokenProvider(
            Tasks.forException(requestException),
        )
        val factory = FakeSmileIDIntegrityManagerFactory(
            Tasks.forResult(tokenProvider),
        )
        setupIntegrityManager(factory)

        // First warm up the provider
        smileIDIntegrityManager.warmUpTokenProvider()

        // When
        val result = smileIDIntegrityManager.requestToken(requestIdentifier)

        // Then
        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Token request failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `requestToken should handle null token response`() = runTest {
        // Given
        val requestIdentifier = "test-request-id"
        val integrityToken = FakeStandardIntegrityToken(null)
        val tokenProvider = FakeStandardIntegrityTokenProvider(
            Tasks.forResult(integrityToken),
        )
        val factory = FakeSmileIDIntegrityManagerFactory(
            Tasks.forResult(tokenProvider),
        )
        setupIntegrityManager(factory)

        // First warm up the provider
        smileIDIntegrityManager.warmUpTokenProvider()

        // When
        val result = smileIDIntegrityManager.requestToken(requestIdentifier)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertNull("Token should be null", result.getOrNull())
    }

    @Test
    fun `requestToken with different identifiers should work correctly`() = runTest {
        // Given
        val identifiers = listOf("request-1", "request-2", "request-3")
        val tokens = listOf("token-1", "token-2", "token-3")

        // Create a custom token provider that returns different tokens
        val tokenProvider = object : StandardIntegrityTokenProvider {
            private var callCount = 0
            override fun request(
                request: StandardIntegrityTokenRequest,
            ): Task<StandardIntegrityToken> =
                Tasks.forResult(FakeStandardIntegrityToken(tokens[callCount++]))
        }

        val factory = FakeSmileIDIntegrityManagerFactory(
            Tasks.forResult(tokenProvider),
        )
        setupIntegrityManager(factory)

        // Warm up first
        smileIDIntegrityManager.warmUpTokenProvider()

        // When
        val results = identifiers.map { identifier ->
            smileIDIntegrityManager.requestToken(identifier)
        }

        // Then
        results.forEachIndexed { index, result ->
            assertTrue("Result $index should be success", result.isSuccess)
            assertEquals(tokens[index], result.getOrNull())
        }
    }

    // Helper method to setup the integrity manager with a custom factory
    private fun setupIntegrityManager(factory: SmileIDIntegrityManagerFactory) {
        every { IntegrityManagerFactory.createStandard(context) } returns factory.create()
        smileIDIntegrityManager = SmileIDStandardRequestIntegrityManager(context)
    }
}

// Test doubles
class FakeSmileIDIntegrityManagerFactory(
    private val prepareTask: Task<StandardIntegrityTokenProvider>,
) : SmileIDIntegrityManagerFactory {
    override fun create(): StandardIntegrityManager = StandardIntegrityManager { prepareTask }
}

class FakeStandardIntegrityTokenProvider(private val requestTask: Task<StandardIntegrityToken>) :
    StandardIntegrityTokenProvider {
    override fun request(request: StandardIntegrityTokenRequest): Task<StandardIntegrityToken> =
        requestTask
}

class FakeStandardIntegrityToken(private val tokenValue: String? = "default-token-123") :
    StandardIntegrityToken() {
    override fun showDialog(activity: Activity?, requestCode: Int): Task<Int> = Tasks.forResult(0)

    override fun token(): String? = tokenValue
}
