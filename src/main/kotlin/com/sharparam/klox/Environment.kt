package com.sharparam.klox

class Environment {
    private val env = HashMap<String, Any?>()

    operator fun get(name: Token) =
            if (env.containsKey(name.lexeme))
                env[name.lexeme]
            else
                throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")

    fun define(name: Token, value: Any?) = set(name, value)

    fun assign(name: Token, value: Any?) =
            if (env.containsKey(name.lexeme))
                this[name] = value
            else
                throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")

    private operator fun set(name: Token, value: Any?) = env.set(name.lexeme, value)
}
