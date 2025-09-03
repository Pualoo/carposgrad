package com.example.myapitest.view.login

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text

        val out = buildAnnotatedString {
            if (digits.isEmpty()) return@buildAnnotatedString

            append("+")
            append(digits.take(2))
            if (digits.length > 2) {
                append(" (")
                append(digits.substring(2).take(2))
            }
            if (digits.length > 4) {
                append(") ")
                append(digits.substring(4).take(5))
            }
            if (digits.length > 9) {
                append("-")
                append(digits.substring(9).take(4))
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset + 1
                    offset <= 4 -> offset + 4
                    offset <= 9 -> offset + 6
                    else -> offset + 7
                }.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 1 -> 0
                    offset <= 4 -> offset - 1
                    offset <= 7 -> 2
                    offset <= 9 -> offset - 4
                    offset <= 12 -> 4
                    offset <= 17 -> offset - 6
                    offset <= 18 -> 9
                    else -> offset - 7
                }.coerceIn(0, digits.length)
            }
        }
        return TransformedText(out, offsetMapping)
    }
}