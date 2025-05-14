package com.smileidentity.util

import java.io.File
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.io.path.createTempDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test

class FileUtilsTest {
    private lateinit var testDir: File
    private val unSubmittedPath = "/unsubmitted"
    private val submittedPath = "/submitted"

    @Before
    fun setup() {
        // Setup a temporary directory for testing
        testDir = createTempDirectory().toFile()

        // Create test structures for submitted and unsubmitted jobs
        val submittedDir = File(testDir, submittedPath).apply { mkdir() }
        val unSubmittedDir = File(testDir, unSubmittedPath).apply { mkdir() }

        // Create dummy files
        (1..3).forEach {
            File(submittedDir, "job_$it").createNewFile()
            File(unSubmittedDir, "job_$it").createNewFile()
        }
    }

    @After
    fun tearDown() {
        // Recursively delete the test directory after each test
        testDir.deleteRecursively()
    }

    @Test
    fun `should clean up all jobs if list is empty`() {
        cleanupJobs(
            deleteSubmittedJobs = true,
            deleteUnsubmittedJobs = true,
            jobIds = listOf(),
            basePaths = mutableListOf(testDir.absolutePath),
        )
        // Assert files are deleted when jobIds is empty
        assertTrue(File(testDir, submittedPath).list()?.isNotEmpty() ?: true)
        assertTrue(File(testDir, unSubmittedPath).list()?.isNotEmpty() ?: true)
    }

    @Test
    fun `should clean up all completed jobs if deleteCompletedJobs is true`() {
        cleanupJobs(
            deleteSubmittedJobs = true,
            jobIds = null,
            basePaths = mutableListOf(testDir.absolutePath),
        )
        // Assert all files in submitted are deleted
        assertTrue(File(testDir, submittedPath).list()?.isEmpty() ?: true)
        // Assert unsubmitted files are untouched
        assertTrue(File(testDir, unSubmittedPath).list()?.isNotEmpty() ?: false)
    }

    @Test
    fun `should clean up all pending jobs if deletePendingJobs is true`() {
        cleanupJobs(
            deleteUnsubmittedJobs = true,
            jobIds = null,
            basePaths = mutableListOf(testDir.absolutePath),
        )
        // Assert all files in unsubmitted are deleted
        assertTrue(File(testDir, unSubmittedPath).list()?.isEmpty() ?: true)
        // Assert submitted files are untouched
        assertTrue(File(testDir, submittedPath).list()?.isNotEmpty() ?: false)
    }

    @Test
    fun `should clean up all jobs using job ids`() {
        val jobIds = listOf("job_1", "job_2")
        cleanupJobs(
            deleteSubmittedJobs = true,
            deleteUnsubmittedJobs = true,
            jobIds = jobIds,
            basePaths = mutableListOf(testDir.absolutePath),
        )
        // Assert specified jobs are deleted from both submitted and unsubmitted
        assertFalse(File(testDir, "$submittedPath/job_1").exists())
        assertFalse(File(testDir, "$submittedPath/job_2").exists())
        assertFalse(File(testDir, "$unSubmittedPath/job_1").exists())
        assertFalse(File(testDir, "$unSubmittedPath/job_2").exists())
        // Assert other jobs remain
        assertTrue(File(testDir, "$submittedPath/job_3").exists())
        assertTrue(File(testDir, "$unSubmittedPath/job_3").exists())
    }

    @Test
    fun `should not clean up all jobs flags are false`() {
        cleanupJobs(
            deleteSubmittedJobs = false,
            deleteUnsubmittedJobs = false,
            jobIds = null,
            basePaths = mutableListOf(testDir.absolutePath),
        )
        // Assert no files are deleted when both flags are false
        File(testDir, submittedPath).list()?.let { assertTrue(it.isNotEmpty()) }
        File(testDir, unSubmittedPath).list()?.let { assertTrue(it.isNotEmpty()) }
    }
}
