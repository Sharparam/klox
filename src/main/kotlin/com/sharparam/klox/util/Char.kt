package com.sharparam.klox.util

internal val Char.isAlpha get() = this in 'a'..'z' || this in 'A'..'Z' || this == '_'

internal val Char.isAlphaNumeric get() = isAlpha || this in '0'..'9'
