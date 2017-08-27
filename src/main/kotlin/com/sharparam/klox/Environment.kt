package com.sharparam.klox

class Environment(private val parent: Environment? = null) {
    private val env = HashMap<String, Any?>()

    operator fun get(name: Token): Any? = when {
        env.containsKey(name.lexeme) -> env[name.lexeme]
        parent != null -> parent[name]
        else -> throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun define(name: Token, value: Any?) = define(name.lexeme, value)

    fun define(key: String, value: Any?) = env.set(key, value)

    fun assign(name: Token, value: Any?): Unit = when {
        env.containsKey(name.lexeme) -> this[name] = value
        parent != null -> parent.assign(name, value)
        else -> throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    private operator fun set(name: Token, value: Any?) = env.set(name.lexeme, value)
}
