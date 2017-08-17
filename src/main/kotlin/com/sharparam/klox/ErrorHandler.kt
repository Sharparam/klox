package com.sharparam.klox

interface ErrorHandler {
    val hadError: Boolean

    fun scanError(token: Token, message: String)

    fun scanError(line: Int, message: String)

    fun parseError(token: Token, message: String)

    fun resetError()
}
