package com.sharparam.klox

class Environment(private val parent: Environment? = null) {
    private val env = HashMap<String, Any?>()

    operator fun get(key: String): Any? = when {
        env.containsKey(key) -> env[key]
        parent != null -> parent[key]
        else -> throw KeyNotFoundError(key)
    }

    operator fun get(name: Token): Any? = try {
        this[name.lexeme]
    } catch (e: KeyNotFoundError) {
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun define(name: Token, value: Any?) = define(name.lexeme, value)

    fun define(key: String, value: Any?) = env.set(key, value)

    fun assign(name: Token, value: Any?): Unit = when {
        env.containsKey(name.lexeme) -> this[name] = value
        parent != null -> parent.assign(name, value)
        else -> throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    private operator fun set(name: Token, value: Any?) = env.set(name.lexeme, value)

    class KeyNotFoundError(@Suppress("unused") val key: String): RuntimeException()
}
