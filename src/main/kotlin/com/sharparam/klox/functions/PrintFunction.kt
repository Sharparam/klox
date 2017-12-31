package com.sharparam.klox.functions

import com.sharparam.klox.Interpreter
import com.sharparam.klox.LoxCallable
import com.sharparam.klox.util.stringify

class PrintFunction : LoxCallable {
    override val arity = 1

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>): Any? {
        println(arguments.first().stringify())
        return null
    }

    companion object {
        const val NAME = "print"
    }
}
