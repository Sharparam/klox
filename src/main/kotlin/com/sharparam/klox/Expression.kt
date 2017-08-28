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

abstract class Expression {
    abstract fun <T> accept(visitor: Visitor<T>): T

    class Assignment(val name: Token, val value: Expression): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Binary(val left: Expression, val operator: Token, val right: Expression): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Call(val callee: Expression, val paren: Token, val arguments: Iterable<Expression>): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Grouping(val expression: Expression): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Literal(val value: Any?): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Logical(val left: Expression, val operator: Token, val right: Expression): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Unary(val operator: Token, val right: Expression): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Variable(val name: Token): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Function(val parameters: Iterable<Token>, val body: Iterable<Statement>): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Conditional(val expression: Expression, val truthy: Expression, val falsey: Expression): Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visit(this)
    }

    interface Visitor<out T> {
        fun visit(expr: Assignment): T
        fun visit(expr: Binary): T
        fun visit(expr: Call): T
        fun visit(expr: Grouping): T
        fun visit(expr: Literal): T
        fun visit(expr: Logical): T
        fun visit(expr: Unary): T
        fun visit(expr: Variable): T
        fun visit(expr: Function): T
        fun visit(expr: Conditional): T
    }
}
