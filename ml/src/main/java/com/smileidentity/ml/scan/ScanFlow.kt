package com.smileidentity.ml.scan

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * A flow for scanning something. This manages the callbacks and lifecycle of the flow.
 */
interface ScanFlow<Parameters, DataType> {

    /**
     * Start the image processing flow for scanning a document or face.
     *
     * @param context: The context used to setup the analyzers
     * @param imageStream: The flow of images to process
     * @param lifecycleOwner: The activity that owns this flow. The flow will pause if the activity
     * is paused
     * @param coroutineScope: The coroutine scope used to run async tasks for this flow
     * @param onError: A handler to report errors to
     */
    fun startFlow(
        context: Context,
        imageStream: Flow<DataType>,
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        parameters: Parameters,
        onError: (e: Exception) -> Unit,
    )

    /**
     * In the event that the scan cannot complete, halt the flow to halt analyzers and free up CPU
     * and memory.
     */
    fun cancelFlow()
}
