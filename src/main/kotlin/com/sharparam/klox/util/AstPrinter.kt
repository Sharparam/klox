package com.sharparam.klox.util

import com.sharparam.klox.Expression

class AstPrinter: Expression.Visitor<String> {
    fun print(expr: Expression): String = expr.accept(this)

    override fun visit(expr: Expression.Assignment) = parenthesize("assign '${expr.name.lexeme}'", expr.value)

    override fun visit(expr: Expression.Binary) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visit(expr: Expression.Grouping) = parenthesize("group", expr.expression)

    override fun visit(expr: Expression.Literal) = expr.value?.toString() ?: "nil"

    override fun visit(expr: Expression.Logical) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visit(expr: Expression.Unary) = parenthesize(expr.operator.lexeme, expr.right)

    override fun visit(expr: Expression.Variable) = parenthesize("var '${expr.name.lexeme}'")

    override fun visit(expr: Expression.Conditional) = parenthesize("cond", expr.expression, expr.truthy, expr.falsey)

    private fun parenthesize(name: String, vararg exprs: Expression): String {
        val builder = StringBuilder().append("(").append(name)

        exprs.forEach {
            builder.append(" ").append(it.accept(this))
        }

        return builder.append(")").toString()
    }
}
