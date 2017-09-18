package com.sharparam.klox.functions

import com.sharparam.klox.Interpreter
import com.sharparam.klox.LoxCallable

class ToNumberFunction : LoxCallable {
    override val arity: Int
        get() = 1

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>): Any? {
        return arguments.first().toString().toDoubleOrNull()
    }

    companion object {
        const val NAME = "tonumber"
    }
}
