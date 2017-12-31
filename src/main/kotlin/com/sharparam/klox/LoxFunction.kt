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

class LoxFunction(
        private val function: Expression.Function,
        private val closure: Environment,
        private val name: String? = null,
        private val isInitializer: Boolean = false
): LoxCallable {
    override val arity: Int
        get() = function.parameters.count()

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>): Any? {
        val environment = Environment(closure)

        (0 until function.parameters.count()).forEach {
            environment.define(function.parameters.elementAt(it), arguments.elementAt(it))
        }

        try {
            interpreter.execute(function.body, environment)
        } catch (e: ReturnException) {
            return e.value
        }

        return if (isInitializer) closure.getAt(0, "this") else null
    }

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(function, environment, name, isInitializer)
    }

    override fun toString() = if (name == null) "<fun>" else "<fun $name>"
}
