package com.smileidentity.util

import com.smileidentity.SmileID
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.PrepUploadRequest
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * The path where unsubmitted (pending) job files are stored.
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
    // Default to the base save path used by createSmileTempFile
    savePath: String = SmileID.fileSavePath,
) {
    if (jobIds != null && jobIds.isEmpty()) return

    val pathsToClean = mutableListOf<String>()
    if (deleteCompletedJobs) pathsToClean.add("$savePath/$SUBMITTED_PATH")
    if (deletePendingJobs) pathsToClean.add("$savePath/$UN_SUBMITTED_PATH")

    if (jobIds == null) {
        // Nuke all files in specified paths
        pathsToClean.forEach { path ->
            File(path).deleteRecursively()
        }
    } else {
        // Delete only specified jobIds within each folder inside the base paths
        pathsToClean.forEach { basePath ->
            File(basePath).walk().forEach { folder ->
                if (folder.isDirectory) {
                    jobIds.forEach { jobId ->
                        File(folder, jobId).deleteRecursively()
                    }
                }
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
    folderName: String,
    fileName: String,
    state: Boolean = true,
    fileExt: String = "jpg",
    savePath: String = SmileID.fileSavePath,
): File {
    val stateDirectory = if (state) UN_SUBMITTED_PATH else SUBMITTED_PATH
    val directory = File(savePath, "$stateDirectory/$folderName")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return File(directory, "si_$fileName.$fileExt")
}

/**
* Constructs a `File` object for a temporary file, ensuring the path and file name adhere to
* expected formats and conditions. This method attempts to address potential edge cases
* related to file path construction and directory accessibility.
*
* @param folderName The name of the folder where the file is saved. Must not be empty and should be a valid folder name.
* @param fileName The base name of the file. Must not be empty and should be a valid file name without special characters.
* @param state Indicates the state directory where the file is stored. True for UN_SUBMITTED_PATH, false for SUBMITTED_PATH.
* @param fileExt The extension of the file, without the leading dot. Defaults to "jpg".
* @param savePath The root directory where the file is saved. Defaults to SmileID.fileSavePath. Must be accessible.
* @return The `File` object representing the exact file.
* @throws IllegalArgumentException If any input parameters are invalid.
* @throws IOException If the directory cannot be created or is not writable.
*/
internal fun getSmileTempFile(
    folderName: String,
    fileName: String,
    state: Boolean = true,
    fileExt: String = "jpg",
    savePath: String = SmileID.fileSavePath,
): File {
    if (folderName.isBlank() || fileName.isBlank() || fileExt.isBlank()) {
        throw IllegalArgumentException(
            "Folder name, file name, and file extension must not be blank.",
        )
    }

    val stateDirectory = if (state) UN_SUBMITTED_PATH else SUBMITTED_PATH
    val directory = File(savePath, "$stateDirectory/$folderName")

    if (!directory.exists() && !directory.mkdirs()) {
        throw IllegalArgumentException("Invalid jobId or not found")
    }

    val fullPath = File(directory, "si_$fileName.$fileExt")

    if (!fullPath.exists()) {
        throw IllegalArgumentException("Invalid file name or not found")
    }
    return fullPath
}

/**
 * Creates a new image file within a specified folder. The file is intended
 * for storing an image of a specified type, such as "jpg" or "png".
 *
 * The function constructs a uniquely named file to avoid naming conflicts
 * and places it within a folder named according to the `folderName` parameter.
 * This is particularly useful for organizing images by type or purpose,
 * facilitating easier management and retrieval of image files.
 *
 * @param imageType The type of the image (e.g., "jpg", "png") which may influence
 *                  the naming convention or processing of the image file.
 * @param folderName The name of the folder in which the new image file will be created.
 *                   This folder is typically a subdirectory of a larger directory
 *                   designated for storing such files.
 * @return [File] An instance of the newly created image file, ready for writing
 *                image data.
 * @throws IOException If an error occurs during file creation, such as insufficient
 *                     permissions or disk space.
 */
internal fun createSmileImageFile(imageType: String, folderName: String): File {
    val fileName = "si_${imageType}_${System.currentTimeMillis()}"
    return createSmileTempFile(fileName, folderName)
}

/**
 * Creates a new JSON file with the specified name within a designated folder. This function
 * is aimed at facilitating the storage of JSON-formatted data, ensuring organized and
 * accessible file management within the application. The use of this function is intended
 * for scenarios where JSON data needs to be persisted locally, such as for configuration
 * settings, user data, or application state.
 *
 * @param fileName The name of the JSON file to be created. This name should include the ".json"
 *                 file extension to indicate the file type clearly.
 * @param folderName The name of the folder within which the JSON file will be created. This
 *                   allows for categorizing and organizing JSON files into specific directories,
 *                   aiding in file management and retrieval.
 * @return [File] An instance of the newly created JSON file, ready for data to be written to.
 *                The returned file object can be used to write JSON data immediately following
 *                file creation.
 * @throws IOException If the file creation process encounters any issues, such as if the
 *                     folder does not exist and cannot be created, or if there is insufficient
 *                     permission to write to the specified directory.
 */
internal fun createSmileJsonFile(fileName: String, folderName: String): File {
    return createSmileTempFile("si_$fileName", folderName, fileExt = "json")
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

internal fun createLivenessFile(jobId: String) = createSmileImageFile("liveness", jobId)
internal fun createSelfieFile(jobId: String) = createSmileImageFile("selfie", jobId)
internal fun createDocumentFile(jobId: String) = createSmileImageFile("document", jobId)

/**
 * Creates a pre-upload file for a specific job and partner parameters.
 *
 * This function generates a file intended to be uploaded before the main job submission.
 * It encapsulates necessary details such as partner parameters and whether new enrollments
 * are allowed. The generated file is stored in a predefined directory structure based on the
 * job's unique identifier (jobId), ensuring each job's pre-upload file is uniquely identifiable
 * and accessible.
 *
 * @param jobId A unique identifier for the job. This is used to determine the storage location
 *              of the pre-upload file and ensure it is associated with the correct job.
 * @param prepUploadRequest An instance of PrepUploadRequest containing the partner parameters
 *             and enrollment allowance flag. This information is serialized and stored in the
 *             pre-upload file for later use during the job submission process.
 * @return A File object pointing to the newly created pre-upload file. The file contains the
 *         serialized partner parameters and enrollment allowance flag, ready for upload or
 *         further processing.
 */
internal fun createPreUploadFile(jobId: String, prepUploadRequest: PrepUploadRequest): File {
    val file = createSmileJsonFile("preupload", jobId)
    file.writeBytes(
        SmileID.moshi.adapter(PrepUploadRequest::class.java)
            .toJson(prepUploadRequest).toByteArray(),
    )
    return file
}

/**
 * Creates an authentication request file for a specific job, incorporating user and job details.
 *
 * This function is responsible for generating a file that contains the authentication request
 * details necessary for processing a job. The request includes information about the job type,
 * whether the job involves enrollment, and identifiers for both the job and the user. The
 * generated file serves as a structured way to encapsulate and transmit authentication details
 * required for job processing.
 *
 * @param jobId A unique identifier for the job. This ID is used to associate the authentication
 *              request file with its corresponding job, ensuring that the authentication
 *              process is tied to the correct job context.
 * @param authRequest A populated instance of AuthenticationRequest containing the necessary
 *              details for the job. This includes the job type, user ID, and other relevant information
 * @return A File object that points to the newly created authentication request file. This file
 *         is structured to include all necessary details for processing the authentication
 *         request and is ready for submission or further action as required by the job's
 *         processing workflow.
 */
internal fun createAuthenticationRequestFile(
    jobId: String,
    authRequest: AuthenticationRequest,
): File {
    authRequest.apply {
        authToken = "" // Remove this so it is not stored offline
    }
    val file = createSmileJsonFile("authenticationrequest", jobId)
    file.writeBytes(
        SmileID.moshi.adapter(AuthenticationRequest::class.java)
            .toJson(authRequest).toByteArray(),
    )
    return file
}

enum class DeleteScope { PendingJobs, CompletedJobs, AllJobs }
