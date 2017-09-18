package com.sharparam.klox.functions

import com.sharparam.klox.Interpreter
import com.sharparam.klox.LoxCallable
import com.sharparam.klox.util.loxType

class TypeFunction : LoxCallable {
    override val arity: Int
        get() = 1

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>): Any? {
        return arguments.first().loxType
    }

    companion object {
        const val NAME = "type"
    }
}
