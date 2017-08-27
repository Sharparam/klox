package com.sharparam.klox

interface LoxCallable {
    val arity: Int get

    operator fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>): Any?
}
