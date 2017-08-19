package com.sharparam.klox

class Environment {
    private val env = HashMap<String, Any?>()

    operator fun get(name: Token) =
            if (env.containsKey(name.lexeme))
                env[name.lexeme]
            else
                throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")

    operator fun set(key: String, value: Any?) {
        env[key] = value
    }

    operator fun set(name: Token, value: Any?) = set(name.lexeme, value)
}
