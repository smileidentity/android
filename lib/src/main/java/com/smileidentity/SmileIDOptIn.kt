package com.smileidentity

/**
 * This exists so that we can re-use UI components from the SDK within the sample app. Doing so has
 * the unfortunate side effect of needing those components to be public and therefore, any consumer
 * of the SDK can also use them. We'd like to discourage this, so we use this annotation to warn any
 * consumers that the API may change without notice and to not rely on it.
 * This should be used conservatively! APIs should be marked as internal/private whenever possible
 */
@RequiresOptIn(
    message = "This API is unsupported. It may be changed in the future without notice.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class SmileIDOptIn
