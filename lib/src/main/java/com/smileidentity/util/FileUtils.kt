package com.smileidentity.util

import com.smileidentity.SmileID
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * The path where unsubmitted job files are stored.
 * Files in this directory are considered in-progress or awaiting submission.
 */
private const val UN_SUBMITTED_PATH = "/pending"

/**
 * The path where submitted (completed) job files are stored.
 * Files in this directory have been processed or marked as completed.
 */
private const val SUBMITTED_PATH = "/complete"

// Enum defining the types of files managed within the job processing system.
// This categorization helps in filtering and processing files based on their content or purpose.

/**
 * Represents the types of files associated with a job. Each type has specific roles
 * within the job processing workflow:
 * - SELFIE: Refers to selfie capture file captured that is pertinent to the job
 * - LIVENESS: Refers to liveness images captured file captured that is pertinent to the job
 * - DOCUMENT:Refers to document capture files captured that is pertinent to the job
 */
enum class FileType {
    SELFIE,
    LIVENESS,
    DOCUMENT,
}

/**
 * Performs cleanup of job files based on specified criteria, allowing selective deletion of files
 * associated with completed and/or pending jobs. This function provides granular control over the
 * cleanup process, enabling targeted deletion operations based on the job's completion status and
 * optionally, specific job IDs.
 *
 * @param deleteCompletedJobs When set to true, the function targets files associated with completed
 * jobs for deletion. This enables clearing out space or managing files that are no longer needed
 * after job completion. Defaults to false to prevent accidental deletion of completed jobs.
 *
 * @param deletePendingJobs When set to true, the function targets files associated with pending
 * jobs for deletion. Useful for resetting or clearing jobs that have not been completed or are
 * no longer needed. Defaults to false to protect ongoing or queued jobs from unintended deletion.
 *
 * @param jobIds An optional list of specific job IDs to delete. If provided, the cleanup process
 * is limited to these IDs, within the context of the completion status flags set. If null, the
 * function applies to all jobs within the specified completion status categories, allowing for
 * bulk cleanup operations.
 *
 * This function directly manipulates the file system by removing files and directories associated
 * with the targeted jobs. It's designed to facilitate efficient storage management and job lifecycle
 * maintenance within the system, ensuring that resources are allocated and used effectively.
 */

internal fun cleanupJobs(
    deleteCompletedJobs: Boolean = false,
    deletePendingJobs: Boolean = false,
    jobIds: List<String>? = null,
) {
    if (jobIds != null && jobIds.isEmpty()) return

    val pathsToClean = mutableListOf<String>()
    if (deleteCompletedJobs) pathsToClean.add(SUBMITTED_PATH)
    if (deletePendingJobs) pathsToClean.add(UN_SUBMITTED_PATH)

    if (jobIds == null) {
        // Nuke all files in specified paths
        pathsToClean.forEach { path ->
            File(path).deleteRecursively()
        }
    } else {
        // Delete only specified jobIds
        pathsToClean.forEach { basePath ->
            jobIds.forEach { jobId ->
                File("$basePath/$jobId").deleteRecursively()
            }
        }
    }
}

/**
 * Initiates the cleanup process for jobs based on their scope and specified job IDs. This function allows for
 * targeted cleanup operations, making it possible to selectively delete job-related files either from completed jobs,
 * pending jobs, or both, depending on the scope provided. If job IDs are specified, the cleanup is restricted to those
 * specific jobs; otherwise, it applies to all jobs within the specified scope.
 *
 * @param scope The scope of the cleanup operation, determined by the DeleteScope enum. The default is DeleteScope.CompletedJobs,
 * indicating that, by default, only completed jobs are targeted for cleanup. This parameter can be adjusted to target
 * pending jobs or both pending and completed jobs, offering flexibility in how cleanup operations are conducted.
 * @param jobIds An optional list of job IDs to specifically target for cleanup. If provided, only the files associated
 * with these job IDs will be cleaned up, within the bounds of the specified scope. If null or not provided, the cleanup
 * operation targets all jobs within the specified scope, allowing for bulk cleanup operations.
 *
 * This function does not return any value, but it directly affects the file system by deleting files associated with
 * the specified jobs. It's designed for internal use within the system to maintain cleanliness and manage storage efficiently.
 */

internal fun cleanupJobs(
    scope: DeleteScope = DeleteScope.CompletedJobs,
    jobIds: List<String>? = null,
) {
    when (scope) {
        DeleteScope.AllJobs -> cleanupJobs(
            deleteCompletedJobs = true,
            deletePendingJobs = true,
            jobIds = jobIds,
        )
        DeleteScope.CompletedJobs -> cleanupJobs(
            deleteCompletedJobs = true,
            deletePendingJobs = false,
            jobIds = jobIds,
        )
        DeleteScope.PendingJobs -> cleanupJobs(
            deleteCompletedJobs = false,
            deletePendingJobs = true,
            jobIds = jobIds,
        )
    }
}

/**
 * Lists the job IDs based on their completion status. This function can retrieve job IDs from both
 * completed and pending categories, depending on the parameters provided. It allows for flexible retrieval,
 * making it suitable for scenarios where either one or both types of job statuses are of interest.
 *
 * @param includeCompleted A boolean flag that, when set to true, includes the IDs of completed jobs
 * in the returned list. Defaults to true to ensure completed jobs are included unless explicitly excluded.
 * @param includePending A boolean flag that, when set to true, includes the IDs of pending jobs
 * in the returned list. Defaults to false, focusing the function on completed jobs unless pending jobs
 * are explicitly requested.
 *
 * @return A list of strings representing the job IDs. The list may include IDs from either the completed
 * or pending categories, or both, based on the flags provided. The order of IDs in the list is determined
 * by the file system's enumeration order and is not guaranteed to follow any specific sorting.
 */

internal fun listJobIds(
    includeCompleted: Boolean = true,
    includePending: Boolean = false,
): List<String> {
    val jobIds = mutableListOf<String>()
    if (includeCompleted) {
        jobIds.addAll(File(SUBMITTED_PATH).list().orEmpty().toList())
    }
    if (includePending) {
        jobIds.addAll(File(UN_SUBMITTED_PATH).list().orEmpty().toList())
    }
    return jobIds
}

/**
 * Retrieves a list of files of a specified type from a given folder, either from submitted or unsubmitted directories.
 * This function filters files based on their prefix which indicates the file type (e.g., "si_selfie_" for selfies),
 * allowing for selective retrieval based on the file's purpose or content.
 *
 * @param folderName The name of the subfolder within the base save path from which to retrieve files. This allows for
 * organization of files by job or category within the broader submitted or unsubmitted directories.
 * @param fileType The type of files to retrieve, specified by the FileType enum. This parameter determines the prefix
 * used to filter files in the directory (SELFIE, LIVENESS, DOCUMENT).
 * @param savePath The base path where files are stored. Defaults to SmileID.fileSavePath, but can be overridden to
 * target different storage locations. Useful for accessing files in environments with multiple storage directories.
 * @param submitted A boolean flag indicating whether to retrieve files from the submitted (true) or unsubmitted (false)
 * directory. This allows the function to adapt based on the processing stage of the files.
 *
 * @return A list of File objects that match the specified type and submission status within the specified folder.
 * The files are filtered and sorted by name to ensure consistent ordering.
 */
fun getFilesByType(
    folderName: String,
    fileType: FileType,
    savePath: String = SmileID.fileSavePath,
    submitted: Boolean = true,
): List<File> {
    val stateDirectory = if (submitted) SUBMITTED_PATH else UN_SUBMITTED_PATH
    val directory = File(savePath, "$stateDirectory/$folderName")

    if (!directory.exists() || !directory.isDirectory) {
        throw IllegalArgumentException("The path provided is not a valid directory.")
    }

    val prefix = when (fileType) {
        FileType.SELFIE -> "si_selfie_"
        FileType.LIVENESS -> "si_liveness_"
        FileType.DOCUMENT -> "si_document_"
    }

    val files = directory.listFiles() ?: emptyArray()
    return files.filter { it.name.startsWith(prefix) }.sortedBy { it.name }
}

/**
 * Save to a file within the app's specific directory under either 'pending' or 'complete' folder.
 * The file format is `si_${imageType}_<timestamp>.jpg` and is saved within a directory named
 * by the jobId.
 * @param imageType The type of the image being saved.
 * @param folderName The id of the job, used to create a sub-directory.
 * @param state The state of the job by default all jobs will go into pending, determining whether
 * to save in 'pending' or 'complete'.
 * @param savePath The base path for saving the file, defaulting to SmileID.fileSavePath.
 * @return The File object pointing to the newly created file.
 */
internal fun createSmileTempFile(
    imageType: String,
    folderName: String,
    state: Boolean = true,
    savePath: String = SmileID.fileSavePath,
): File {
    val stateDirectory = if (state) UN_SUBMITTED_PATH else SUBMITTED_PATH
    val directory = File(savePath, "$stateDirectory/$folderName")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return File(directory, "si_${imageType}_${System.currentTimeMillis()}.jpg")
}

/**
 * Moves a folder from 'pending' to 'complete' within the app's specific directory, handling all
 * edge cases.
 * @param folderName The name of the job or operation, corresponding to the folder to be moved.
 * @param savePath The base path where the 'pending' and 'complete' folders are
 * located, defaulting to SmileID.fileSavePath.
 * @return A Boolean indicating whether the move operation was successful.
 */
internal fun moveJobToComplete(
    folderName: String,
    savePath: String = SmileID.fileSavePath,
): Boolean {
    val pendingPath = File(savePath, "$UN_SUBMITTED_PATH/$folderName")
    val completePath = File(savePath, "$SUBMITTED_PATH/$folderName")

    if (!pendingPath.exists() || !pendingPath.isDirectory) {
        println("Source directory does not exist or is not a directory")
        return false
    }

    if (!completePath.exists() && !completePath.mkdirs()) {
        println("Failed to create target directory")
        return false
    }

    try {
        pendingPath.walk().forEach { sourceFile ->
            val relativePath = sourceFile.toRelativeString(pendingPath)
            val targetFile = File(completePath, relativePath)

            if (sourceFile.isDirectory) {
                if (!targetFile.exists() && !targetFile.mkdirs()) {
                    throw IOException("Failed to create directory ${targetFile.path}")
                }
            } else {
                sourceFile.copyTo(targetFile, true)
                if (!sourceFile.delete()) {
                    throw IOException(
                        "Failed to delete original file ${sourceFile.path} after copy",
                    )
                }
            }
        }
        // Check and delete the now-empty source directory
        if (!pendingPath.deleteRecursively()) {
            throw IOException("Failed to delete the source directory ${pendingPath.path}")
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return false
    }

    return true
}

/**
 * Utility method to copy a file using FileChannel for efficiency.
 */
private fun File.copyTo(target: File, overwrite: Boolean) {
    if (!overwrite && target.exists()) {
        throw IOException("File ${target.path} already exists and overwrite is false")
    }

    FileInputStream(this).channel.use { sourceChannel ->
        FileOutputStream(target).channel.use { targetChannel ->
            sourceChannel.transferTo(0, sourceChannel.size(), targetChannel)
        }
    }
}

internal fun createLivenessFile(jobId: String) = createSmileTempFile("liveness", jobId)
internal fun createSelfieFile(jobId: String) = createSmileTempFile("selfie", jobId)
internal fun createDocumentFile(jobId: String) = createSmileTempFile("document", jobId)

enum class DeleteScope { PendingJobs, CompletedJobs, AllJobs }
