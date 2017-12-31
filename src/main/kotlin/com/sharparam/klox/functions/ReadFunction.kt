package com.sharparam.klox.functions

import com.sharparam.klox.Interpreter
import com.sharparam.klox.LoxCallable

class ReadFunction : LoxCallable {
    override val arity = 0

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>) = readLine()

    companion object {
        const val NAME = "read"
    }
}
