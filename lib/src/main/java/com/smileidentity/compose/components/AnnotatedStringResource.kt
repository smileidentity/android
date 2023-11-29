package com.smileidentity.compose.components

import android.text.Annotation
import android.text.SpannedString
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.core.text.getSpans
import com.smileidentity.util.SpanFormatter

@Composable
@ReadOnlyComposable
fun annotatedStringResource(
    @StringRes id: Int,
    vararg formatArgs: Any,
    spanStyles: (Annotation) -> SpanStyle? = { null },
): AnnotatedString {
    // Using resources.getText() instead of stringResource() in order to preserve Spans
    val resources = LocalContext.current.resources
    val spannedString = SpanFormatter.format(SpannedString(resources.getText(id)), *formatArgs)
    val resultBuilder = AnnotatedString.Builder()
    resultBuilder.append(spannedString.toString())
    spannedString.getSpans<Annotation>().forEach { annotation ->
        val spanStart = spannedString.getSpanStart(annotation)
        val spanEnd = spannedString.getSpanEnd(annotation)
        resultBuilder.addStringAnnotation(
            tag = annotation.key,
            annotation = annotation.value,
            start = spanStart,
            end = spanEnd,
        )
        spanStyles(annotation)?.let { resultBuilder.addStyle(it, spanStart, spanEnd) }
    }
    return resultBuilder.toAnnotatedString()
}
