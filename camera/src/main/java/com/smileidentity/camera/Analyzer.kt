package com.smileidentity.camera

/**
 * An analyzer takes some data as an input, and returns an analyzed output. Analyzers should not
 * contain any state. They must define whether they can run on a multithreaded executor, and provide
 * a means of analyzing input data to return some form of result.
 */
interface Analyzer<Input, State, Output> {
    suspend fun analyze(data: Input, state: State): Output
}

/**
 * A factory to create analyzers.
 */

interface AnalyzerFactory<
    Input,
    State,
    Output,
    AnalyzerType :
    Analyzer<Input, State, Output>,
    > {
    suspend fun newInstance(): AnalyzerType?
}
