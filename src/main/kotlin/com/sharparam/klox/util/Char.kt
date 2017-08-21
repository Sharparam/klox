package com.sharparam.klox.util

val Char.isAlpha get() = this in 'a'..'z' || this in 'A'..'Z' || this == '_'

val Char.isAlphaNumeric get() = isAlpha || this in '0'..'9'