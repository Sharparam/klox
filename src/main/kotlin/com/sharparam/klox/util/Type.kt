package com.sharparam.klox.util

import com.sharparam.klox.LoxCallable

internal fun Any?.stringify(): String = when (this) {
    null -> "nil"

    is Double -> {
        val text = this.toString()
        if (text.endsWith(".0"))
            text.substring(0, text.length - 2)
        else
            text
    }

    else -> this.toString()
}

internal val Any?.loxType: String get() = when (this) {
    is String -> "string"
    is Double -> "number"
    is Boolean -> "bool"
    is LoxCallable -> "function"
    null -> "nil"
    else -> "undefined"
}
