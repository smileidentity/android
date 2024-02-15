package com.smileidentity.networking

import com.smileidentity.models.JobStatusResponse
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmileIDServiceTest {
    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
    @Test
    fun `poll should wait between attempts`() = runTest {
        // given
        val delay = 1.seconds
        val incompleteResponse = mockk<JobStatusResponse> { every { jobComplete } returns false }
        val completeResponse = mockk<JobStatusResponse> { every { jobComplete } returns true }
        val responses = listOf(incompleteResponse, incompleteResponse, completeResponse)
        val expectedTotalTime = delay * (responses.size - 1)

        // when
        val duration = testTimeSource.measureTime {
            poll(delay, Int.MAX_VALUE) { responses[it] }.collect { }
        }

        // then
        assertEquals(expectedTotalTime, duration)
    }

    @Test
    fun `poll should stop after numAttempts`() = runTest {
        // given
        val numAttempts = 2
        val incompleteResponse = mockk<JobStatusResponse> { every { jobComplete } returns false }
        val responses = listOf(incompleteResponse, incompleteResponse, incompleteResponse)
        var counter = 0

        // when
        poll(1.seconds, numAttempts) { responses[it] }.collect { counter++ }

        // then
        assertEquals(numAttempts, counter)
    }

    @Test
    fun `poll should stop after job is complete`() = runTest {
        // given
        val incompleteResponse = mockk<JobStatusResponse> { every { jobComplete } returns false }
        val completeResponse = mockk<JobStatusResponse> { every { jobComplete } returns true }
        // the last incomplete response should never be emitted
        val responses = listOf(incompleteResponse, completeResponse, incompleteResponse)

        // when
        val lastResult = poll(1.seconds, Int.MAX_VALUE) { responses[it] }.toList().last()

        // then
        assertTrue(lastResult.jobComplete)
    }

    @Test
    fun `poll should throw if attempts fail`() = runTest {
        // given
        val expectedException = Exception("test")

        // when
        val error = try {
            poll(1.seconds, 100) { throw expectedException }.collect { }
            null
        } catch (e: Exception) {
            e
        }

        // then
        assertEquals(expectedException.message, error?.message)
    }
}
