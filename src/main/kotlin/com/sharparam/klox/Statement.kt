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

abstract class Statement {
    abstract fun <T> accept(visitor: Visitor<T>): T

    class Expression(val expression: com.sharparam.klox.Expression): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Function(val name: Token, val function: com.sharparam.klox.Expression.Function): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class If(val condition: com.sharparam.klox.Expression, val thenStmt: Statement, val elseStmt: Statement?): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Variable(val name: Token, val initializer: com.sharparam.klox.Expression): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Return(val keyword: Token, val value: com.sharparam.klox.Expression?): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class While(val condition: com.sharparam.klox.Expression, val body: Statement): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class For(
            val init: Statement?,
            val condition: com.sharparam.klox.Expression,
            val increment: Statement?,
            val body: Statement
    ): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Block(val statements: Iterable<Statement>): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Break: Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Continue: Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    interface Visitor<out T> {
        fun visit(stmt: Expression): T
        fun visit(stmt: Function): T
        fun visit(stmt: If): T
        fun visit(stmt: Variable): T
        fun visit(stmt: Return): T
        fun visit(stmt: While): T
        fun visit(stmt: For): T
        fun visit(stmt: Block): T
        fun visit(stmt: Break): T
        fun visit(stmt: Continue): T
    }
}
