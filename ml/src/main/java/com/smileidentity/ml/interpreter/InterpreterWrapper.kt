package com.smileidentity.ml.interpreter

import androidx.annotation.RestrictTo
import java.io.File

/**
 * Wrapper for TFLite interpreter API.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface InterpreterWrapper {
    fun runForMultipleInputsOutputs(
        inputs: Array<Any>,
        outputs: Map<Int, Any>
    )

    fun run(input: Any, output: Any)

    fun close()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class InterpreterWrapperImpl constructor(file: File, options: InterpreterOptionsWrapper) : InterpreterWrapper {
    private val interpreter: Interpreter = Interpreter(file, options.toInterpreterOptions())

    override fun runForMultipleInputsOutputs(inputs: Array<Any>, outputs: Map<Int, Any>) {
        interpreter.runForMultipleInputsOutputs(inputs, outputs)
    }

    override fun run(input: Any, output: Any) {
        interpreter.run(input, output)
    }

    override fun close() {
        interpreter.close()
    }
}

private fun InterpreterOptionsWrapper.toInterpreterOptions(): Interpreter.Options {
    val ret = Interpreter.Options()
    useNNAPI?.let {
        ret.setUseNNAPI(it)
    }
    numThreads?.let {
        ret.setNumThreads(it)
    }
    return ret
}
