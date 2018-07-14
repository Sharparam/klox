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

package com.sharparam.klox.util

import com.sharparam.klox.Expression

class AstPrinter: Expression.Visitor<String> {
    fun print(expr: Expression): String = expr.accept(this)

    override fun visit(expr: Expression.Assignment) = parenthesize("assign '${expr.name.lexeme}'", expr.value)

    override fun visit(expr: Expression.Get) = parenthesize("get '${expr.name.lexeme}'")

    override fun visit(expr: Expression.Set) = parenthesize("set '${expr.name.lexeme}'", expr.value)

    override fun visit(expr: Expression.This) = "this"

    override fun visit(expr: Expression.Super) = "super.${expr.method.lexeme}"

    override fun visit(expr: Expression.Binary) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visit(expr: Expression.Call): String {
        val exprs = ArrayList<Expression>(1 + expr.arguments.count())

        exprs.add(expr.callee)
        expr.arguments.forEach { exprs.add(it) }

        return parenthesize("call", *exprs.toTypedArray())
    }

    override fun visit(expr: Expression.Grouping) = parenthesize("group", expr.expression)

    override fun visit(expr: Expression.Literal) = expr.value?.toString() ?: "nil"

    override fun visit(expr: Expression.Logical) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visit(expr: Expression.Unary) = parenthesize(expr.operator.lexeme, expr.right)

    override fun visit(expr: Expression.Variable) = parenthesize("var '${expr.name.lexeme}'")

    override fun visit(expr: Expression.Conditional) = parenthesize("cond", expr.expression, expr.truthy, expr.falsey)

    override fun visit(expr: Expression.Function) = parenthesize(
            "function${expr.parameters.joinToString(" ", prefix = " ") { it.lexeme }}"
    )

    private fun parenthesize(name: String, vararg exprs: Expression): String {
        val builder = StringBuilder().append("(").append(name)

        exprs.forEach {
            builder.append(" ").append(it.accept(this))
        }

        return builder.append(")").toString()
    }
}
