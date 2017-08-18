package com.sharparam.klox

interface ErrorHandler {
    val hadError: Boolean

    val hadRuntimeError: Boolean

    fun scanError(token: Token, message: String)

    fun scanError(line: Int, column: Int, message: String)

    fun parseError(token: Token, message: String)

    fun runtimeError(e: RuntimeError)

    fun resetError()
}
