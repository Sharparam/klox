package com.sharparam.klox.functions

import com.sharparam.klox.Interpreter
import com.sharparam.klox.LoxCallable
import com.sharparam.klox.util.stringify

class ToStringFunction : LoxCallable {
    override val arity = 1

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>) =
            arguments.first().stringify()

    companion object {
        const val NAME = "tostring"
    }
}
