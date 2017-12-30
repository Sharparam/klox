/*
 * Copyright (c) 2017 by Adam Hellberg.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.sharparam.klox

import kotlin.repeat

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

    fun getAt(distance: Int, name: Token): Any? {
        var environment: Environment = this
        repeat(distance) {
            if (environment.parent == null)
                throw KloxError("Environment parent was NULL during resolve")

            environment = environment.parent!!
        }
        return environment[name]
    }

    fun define(name: Token, value: Any?) = define(name.lexeme, value)

    fun define(key: String, value: Any?) = env.set(key, value)

    fun assign(name: Token, value: Any?): Unit = when {
        env.containsKey(name.lexeme) -> this[name] = value
        parent != null -> parent.assign(name, value)
        else -> throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        var environment = this
        repeat(distance) {
            if (environment.parent == null)
                throw KloxError("Environment parent was NULL during resolve")

            environment = environment.parent!!
        }
        environment.assign(name, value)
    }

    private operator fun set(name: Token, value: Any?) = env.set(name.lexeme, value)

    class KeyNotFoundError(@Suppress("unused") val key: String): RuntimeException()
}
