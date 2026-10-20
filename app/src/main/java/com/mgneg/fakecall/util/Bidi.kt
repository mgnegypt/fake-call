package com.mgneg.fakecall.util

import android.text.BidiFormatter
import android.text.TextDirectionHeuristics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** Wraps a phone number / URL so it always renders LTR inside RTL text. */
object Bidi {
    fun ltr(text: String): String =
        BidiFormatter.getInstance().unicodeWrap(text, TextDirectionHeuristics.LTR)
}

@Composable
fun rememberLtr(text: String): String = remember(text) { Bidi.ltr(text) }
