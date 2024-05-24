package com.smileidentity.util

import com.smileidentity.SmileID
import com.smileidentity.SmileIDCrashReporting
import com.smileidentity.models.AuthenticationRequest
import com.smileidentity.models.PrepUploadRequest
import com.smileidentity.models.UploadRequest
import java.io.File
import java.io.IOException
import okio.buffer
import okio.sink
import timber.log.Timber

/**
 * The path where unsubmitted job files are stored.
 * Files in this directory are considered in-progress or awaiting submission.
 */
private const val UNSUBMITTED_PATH = "/unsubmitted"

/**
 * The path where submitted job files are stored.
 * Files in this directory have been processed or marked as completed.
 */
private const val SUBMITTED_PATH = "/submitted"

// File names
const val AUTH_REQUEST_FILE = "authentication_request.json"
const val PREP_UPLOAD_REQUEST_FILE = "prep_upload.json"
const val UPLOAD_REQUEST_FILE = "info.json"

// Enum defining the types of files managed within the job processing system.
// This categorization helps in filtering and processing files based on their content or purpose.

/**
 * Represents the types of files associated with a job. Each type has specific roles
 * within the job processing workflow:
 * - SELFIE: Refers to selfie capture file captured that is pertinent to the job
 * - LIVENESS: Refers to liveness images captured file captured that is pertinent to the job
 * - DOCUMENT:Refers to document capture files captured that is pertinent to the job
 */
enum class FileType(val fileType: String) {
    SELFIE("si_selfie_"),
    LIVENESS("si_liveness_"),
    DOCUMENT_FRONT("si_document_front_"),
    DOCUMENT_BACK("si_document_back_"),
    ;

    override fun toString() = fileType
}

/**
 * Performs cleanup of job files based on specified criteria, allowing selective deletion of files
 * associated with completed and/or pending jobs. This function provides granular control over the
 * cleanup process, enabling targeted deletion operations based on the job's completion status and
 * optionally, specific job IDs.
 *
 * @param deleteSubmittedJobs When set to true, the function targets files associated with completed
 * jobs for deletion. This enables clearing out space or managing files that are no longer needed
 * after job completion. Defaults to false to prevent accidental deletion of completed jobs.
 *
 * @param deleteUnsubmittedJobs When set to true, the function targets files associated with
 * unsubmitted jobs for deletion. Useful for resetting or clearing jobs that have not been completed
 * or are no longer needed. Defaults to false to protect ongoing or queued jobs from unintended
 * deletion.
 *
 * @param jobIds An optional list of specific job IDs to delete. If provided, the cleanup process
 * is limited to these IDs, within the context of the completion status flags set. If null, the
 * function applies to all jobs within the specified completion status categories, allowing for
 * bulk cleanup operations.
 *
 * This function directly manipulates the file system by removing files and directories associated
 * with the targeted jobs. It's designed to facilitate efficient storage management and job
 * lifecycle maintenance within the system, ensuring that resources are allocated and used
 * effectively.
 */
internal fun cleanupJobs(
    deleteSubmittedJobs: Boolean = false,
    deleteUnsubmittedJobs: Boolean = false,
    jobIds: List<String>? = null,
    // Default to the base save path used by createSmileTempFile
    savePath: String = SmileID.fileSavePath,
) {
    val pathsToClean = mutableListOf<String>()
    if (deleteSubmittedJobs) pathsToClean.add("$savePath/$SUBMITTED_PATH")
    if (deleteUnsubmittedJobs) pathsToClean.add("$savePath/$UNSUBMITTED_PATH")

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
 * Initiates the cleanup process for jobs based on their scope and specified job IDs. This function
 * allows for targeted cleanup operations, making it possible to selectively delete job-related
 * files either from completed jobs, pending jobs, or both, depending on the scope provided. If job
 * IDs are specified, the cleanup is restricted to those specific jobs; otherwise, it applies to all
 * jobs within the specified scope.
 *
 * @param scope The scope of the cleanup operation, determined by the DeleteScope enum. The default
 * is DeleteScope.All, indicating that, by default, all jobs are targeted for cleanup. This
 * parameter can be adjusted to target pending jobs or both pending and completed jobs, offering
 * flexibility in how cleanup operations are conducted.
 * @param jobIds An optional list of job IDs to specifically target for cleanup. If provided, only
 * the files associated with these job IDs will be cleaned up, within the bounds of the specified
 * scope. If null or not provided, the cleanup operation targets all jobs within the specified
 * scope, allowing for bulk cleanup operations.
 *
 * This function does not return any value, but it directly affects the file system by deleting
 * files associated with the specified jobs. It's designed for internal use within the system to
 * maintain cleanliness and manage storage efficiently.
 */

internal fun cleanupJobs(scope: DeleteScope = DeleteScope.All, jobIds: List<String>? = null) {
    when (scope) {
        DeleteScope.All -> cleanupJobs(
            deleteSubmittedJobs = true,
            deleteUnsubmittedJobs = true,
            jobIds = jobIds,
        )

        DeleteScope.Submitted -> cleanupJobs(
            deleteSubmittedJobs = true,
            deleteUnsubmittedJobs = false,
            jobIds = jobIds,
        )

        DeleteScope.Unsubmitted -> cleanupJobs(
            deleteSubmittedJobs = false,
            deleteUnsubmittedJobs = true,
            jobIds = jobIds,
        )
    }
}

/**
 * Lists only the subdirectories of a given directory.
 *
 * @param rootDir The root directory to walk through.
 * @return A list of File objects representing the subdirectories.
 */
private fun listSubdirectories(rootDir: File): List<File> {
    // Check if rootDir is a directory
    if (!rootDir.isDirectory) {
        throw IllegalArgumentException("The provided path is not a directory.")
    }

    // Filter only directories
    return rootDir.listFiles { file -> file.isDirectory }.orEmpty().toList()
}

internal fun doGetUnsubmittedJobs(): List<String> {
    return listJobIds(includeSubmitted = false, includeUnsubmitted = true)
}

internal fun doGetSubmittedJobs(): List<String> {
    return listJobIds(includeSubmitted = true, includeUnsubmitted = false)
}

/**
 * Lists the job IDs based on their completion status. This function can retrieve job IDs from both
 * completed and pending categories, depending on the parameters provided. It allows for flexible
 * retrieval, making it suitable for scenarios where either one or both types of job statuses are of
 * interest.
 *
 * @param includeSubmitted A boolean flag that, when set to true, includes the IDs of completed jobs
 * in the returned list. Defaults to true to ensure completed jobs are included unless explicitly
 * excluded.
 * @param includeUnsubmitted A boolean flag that, when set to true, includes the IDs of pending jobs
 * in the returned list. Defaults to false, focusing the function on completed jobs unless pending
 * jobs are explicitly requested.
 *
 * @return A list of strings representing the job IDs. The list may include IDs from either the
 * completed or pending categories, or both, based on the flags provided. The order of IDs in the
 * list is determined by the file system's enumeration order and is not guaranteed to follow any
 * specific sorting.
 */
private fun listJobIds(
    includeSubmitted: Boolean = true,
    includeUnsubmitted: Boolean = false,
    savePath: String = SmileID.fileSavePath,
): List<String> {
    val jobIds = mutableListOf<String>()
    val pathsToInclude = mutableListOf<String>()

    if (includeSubmitted) pathsToInclude.add("$savePath/$SUBMITTED_PATH")
    if (includeUnsubmitted) pathsToInclude.add("$savePath/$UNSUBMITTED_PATH")

    pathsToInclude.forEach { path ->
        val dir = File(path)
        if (dir.exists() && dir.isDirectory) {
            val names = dir.list()?.toList().orEmpty()
            jobIds.addAll(names)
        }
    }

    return jobIds.distinct()
}

/**
 * Retrieves a file of a specified type from a given folder, either from the submitted or
 * unsubmitted directory. This function filters files based on their prefix which indicates the file
 * type (e.g., "si_selfie_" for selfies), allowing for selective retrieval based on the file's
 * purpose or content.
 *
 * @param folderName The name of the subfolder within the base save path from which to retrieve
 * files. This allows for organization of files by job or category within the broader submitted or
 * unsubmitted directories.
 * @param fileType The type of file to retrieve, specified by the FileType enum. This parameter
 * determines the prefix used to filter files in the directory (SELFIE, LIVENESS, DOCUMENT_FRONT,
 * DOCUMENT_BACK).
 * @param savePath The base path where files are stored. Defaults to SmileID.fileSavePath, but can
 * be overridden to target different storage locations. Useful for accessing files in environments
 * with multiple storage directories.
 * @param submitted A boolean flag indicating whether to retrieve files from the submitted (true) or
 * unsubmitted (false) directory. This allows the function to adapt based on the processing stage of
 * the files.
 *
 * @return A File object that matches the specified type and submission status within the specified
 * folder. The file is filtered and sorted by name to ensure consistent ordering.
 */
fun getFileByType(
    folderName: String,
    fileType: FileType,
    savePath: String = SmileID.fileSavePath,
    submitted: Boolean = true,
): File? {
    return getFilesByType(
        folderName,
        fileType,
        savePath,
        submitted,
    ).firstOrNull()
}

/**
 * Retrieves a list of files of a specified type from a given folder, either from submitted or
 * unsubmitted directories. This function filters files based on their prefix which indicates the
 * file type (e.g., "si_selfie_" for selfies), allowing for selective retrieval based on the file's
 * purpose or content.
 *
 * @param folderName The name of the subfolder within the base save path from which to retrieve
 * files. This allows for organization of files by job or category within the broader submitted or
 * unsubmitted directories.
 * @param fileType The type of files to retrieve, specified by the FileType enum. This parameter
 * determines the prefix used to filter files in the directory (Selfie, Liveness, DOCUMENT).
 * @param savePath The base path where files are stored. Defaults to SmileID.fileSavePath, but can
 * be overridden to target different storage locations. Useful for accessing files in environments
 * with multiple storage directories.
 * @param submitted A boolean flag indicating whether to retrieve files from the submitted (true) or
 * unsubmitted (false) directory. This allows the function to adapt based on the processing stage of
 * the files.
 *
 * @return A list of File objects that match the specified type and submission status within the
 * specified folder. The files are filtered and sorted by name to ensure consistent ordering.
 */
fun getFilesByType(
    folderName: String,
    fileType: FileType,
    savePath: String = SmileID.fileSavePath,
    submitted: Boolean = true,
): List<File> {
    val stateDirectory = if (submitted) SUBMITTED_PATH else UNSUBMITTED_PATH
    val directory = File(savePath, "$stateDirectory/$folderName")

    if (!directory.exists() || !directory.isDirectory) {
        Timber.w("The path provided is not a valid directory.")
        throw IllegalArgumentException("The path provided is not a valid directory.")
    }
    val files = directory.listFiles() ?: emptyArray()
    return files.filter { it.name.startsWith(fileType.fileType) }.sortedBy { it.name }
}

/**
 * Save to a file within the app's specific directory under either 'pending' or 'complete' folder.
 * The file format is `si_${imageType}_<timestamp>.jpg` and is saved within a directory named
 * by the jobId.
 * @param fileName The type of the image being saved.
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
    savePath: String = SmileID.fileSavePath,
): File {
    val stateDirectory = if (state) UNSUBMITTED_PATH else SUBMITTED_PATH
    val directory = File(savePath, "$stateDirectory/$folderName")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    return File(directory, fileName)
}

/**
 * Constructs a `File` object for a temporary file, ensuring the path and file name adhere to
 * expected formats and conditions. This method attempts to address potential edge cases related to
 * file path construction and directory accessibility.
 *
 * @param folderName The name of the folder where the file is saved. Must not be empty and should be
 * a valid folder name.
 * @param fileName The base name of the file. Must not be empty and should be a valid file name
 * without special characters.
 * @param isUnsubmitted Indicates the isUnsubmitted directory where the file is stored. True for
 * UNSUBMITTED_PATH, false for SUBMITTED_PATH.
 * @param savePath The root directory where the file is saved. Defaults to SmileID.fileSavePath.
 * Must be accessible.
 * @return The `File` object representing the exact file.
 * @throws IllegalArgumentException If any input parameters are invalid.
 * @throws IOException If the directory cannot be created or is not writable.
 */
internal fun getSmileTempFile(
    folderName: String,
    fileName: String,
    isUnsubmitted: Boolean = true,
    savePath: String = SmileID.fileSavePath,
): File {
    if (folderName.isBlank() || fileName.isBlank()) {
        throw IllegalArgumentException(
            "Folder name, file name, and file extension must not be blank.",
        )
    }

    val stateDirectory = if (isUnsubmitted) UNSUBMITTED_PATH else SUBMITTED_PATH
    val directory = File(savePath, "$stateDirectory/$folderName")

    if (!directory.exists() && !directory.mkdirs()) {
        throw IllegalArgumentException("Invalid jobId or not found")
    }

    val fullPath = File(directory, fileName)

    if (!fullPath.exists()) {
        throw IllegalArgumentException("Invalid file name or not found")
    }
    return fullPath
}

/**
 * Creates a new image file within a specified folder. The file is intended
 * for storing an image of a specified type, such as selfie or liveness
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
internal fun createSmileImageFile(imageType: FileType, folderName: String): File {
    val fileName = "${imageType.fileType}${System.currentTimeMillis()}.jpg"
    return createSmileTempFile(folderName, fileName)
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
    return createSmileTempFile(folderName, fileName)
}

/**
 * Moves a folder from 'unsubmitted' to 'submitted' within the app's specific directory, handling
 * all edge cases. We copy files recursively as opposed to moving the entire folder so that we can
 * merge contents (i.e. in the case something got moved to submitted but was later retried - this
 * can happen in the case when Offline Mode is disabled, but there was an error and the user
 * retries).
 *
 * @param folderName The name of the job or operation, corresponding to the folder to be moved.
 * @param savePath The base path where the 'pending' and 'complete' folders are
 * located, defaulting to SmileID.fileSavePath.
 * @return A Boolean indicating whether the move operation was successful.
 */
internal fun moveJobToSubmitted(
    folderName: String,
    savePath: String = SmileID.fileSavePath,
): Boolean {
    val unSubmittedPath = File(savePath, "$UNSUBMITTED_PATH/$folderName")
    val submittedPath = File(savePath, "$SUBMITTED_PATH/$folderName")

    if (!unSubmittedPath.exists() || !unSubmittedPath.isDirectory) {
        val message = "Unsubmitted directory does not exist or is not a directory"
        Timber.v(message)
        SmileIDCrashReporting.hub.addBreadcrumb(message)
        return false
    }

    try {
        unSubmittedPath.walk().filter { it.isFile && it.extension == "json" }.forEach { file ->
            if (!file.delete()) {
                throw IOException("Failed to delete JSON file ${file.path}")
            }
        }
        if (unSubmittedPath.copyRecursively(submittedPath, overwrite = true)) {
            // After successfully deleting JSON files, delete the original directory if empty or any
            // remaining files
            if (!unSubmittedPath.deleteRecursively()) {
                throw IOException(
                    "Failed to delete the source directory or " +
                        "some files within it ${unSubmittedPath.path}",
                )
            }
        } else {
            throw IOException("Failed to copy files to the target directory ${submittedPath.path}")
        }
    } catch (e: IOException) {
        Timber.w(e, "Failed to move job to submitted")
        return false
    }

    return true
}

internal fun createLivenessFile(jobId: String) = createSmileImageFile(FileType.LIVENESS, jobId)
internal fun createSelfieFile(jobId: String) = createSmileImageFile(FileType.SELFIE, jobId)
internal fun createDocumentFile(jobId: String, isFront: Boolean) = createSmileImageFile(
    if (isFront) {
        FileType.DOCUMENT_FRONT
    } else {
        FileType.DOCUMENT_BACK
    },
    jobId,
)

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
internal fun createPrepUploadFile(jobId: String, prepUploadRequest: PrepUploadRequest): File {
    val file = createSmileJsonFile(PREP_UPLOAD_REQUEST_FILE, jobId)
    file.sink().buffer().use { sink ->
        SmileID.moshi.adapter(PrepUploadRequest::class.java).toJson(sink, prepUploadRequest)
    }
    return file
}

internal fun createUploadRequestFile(jobId: String, uploadRequest: UploadRequest): File {
    val file = createSmileJsonFile(UPLOAD_REQUEST_FILE, jobId)
    file.sink().buffer().use { sink ->
        SmileID.moshi.adapter(UploadRequest::class.java).toJson(sink, uploadRequest)
    }
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
 *                    details for the job. This includes the job type, user ID, and other relevant
 *                    information
 * @return A File object that points to the newly created authentication request file. This file
 *         is structured to include all necessary details for processing the authentication
 *         request and is ready for submission or further action as required by the job's
 *         processing workflow.
 */
internal fun createAuthenticationRequestFile(
    jobId: String,
    authRequest: AuthenticationRequest,
): File {
    val file = createSmileJsonFile(AUTH_REQUEST_FILE, jobId)
    file.sink().buffer().use { sink ->
        SmileID.moshi.adapter(AuthenticationRequest::class.java)
            .toJson(sink, authRequest.copy(authToken = ""))
    }
    return file
}

enum class DeleteScope { Unsubmitted, Submitted, All }
