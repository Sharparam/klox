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

class LoxClass(val name: String, val superclass: LoxClass?, private val methods: Map<String, LoxFunction>): LoxCallable {
    override val arity: Int = methods["init"]?.arity ?: 0

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>): Any? {
        val instance = LoxInstance(this)
        if (hasMethod("init"))
            getMethod("init", instance)?.invoke(interpreter, arguments)
        return instance
    }

    fun hasMethod(key: String): Boolean = methods.containsKey(key) || (superclass?.hasMethod(key) ?: false)

    fun getMethod(key: String, instance: LoxInstance): LoxFunction? =
            methods[key]?.bind(instance) ?: superclass?.getMethod(key, instance)

    override fun toString() = "<class $name>"
}
