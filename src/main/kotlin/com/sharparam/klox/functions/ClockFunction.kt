package com.sharparam.klox.functions

import com.sharparam.klox.Interpreter
import com.sharparam.klox.LoxCallable

class ClockFunction : LoxCallable {
    override val arity: Int
        get() = 0

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>): Any? {
        return System.currentTimeMillis() / 1000.0
    }

    companion object {
        const val NAME = "clock"
    }
}
