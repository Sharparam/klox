package com.sharparam.klox.functions

import com.sharparam.klox.Interpreter
import com.sharparam.klox.LoxCallable

class ToNumberFunction : LoxCallable {
    override val arity = 1

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>) =
            arguments.first().toString().toDoubleOrNull()

    companion object {
        const val NAME = "tonumber"
    }
}
