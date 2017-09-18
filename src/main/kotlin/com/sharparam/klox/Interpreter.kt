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

import com.sharparam.klox.functions.*
import com.sharparam.klox.util.stringify

class Interpreter(private val errorHandler: ErrorHandler) : Expression.Visitor<Any?>, Statement.Visitor<Unit> {
    internal val globals = Environment()

    private var environment = globals

    private val Any?.isTruthy: Boolean get() = when (this) {
        null -> false
        is Boolean -> this
        else -> true
    }

    init {
        environment.define(TypeFunction.NAME, TypeFunction())
        environment.define(ClockFunction.NAME, ClockFunction())
        environment.define(PrintFunction.NAME, PrintFunction())
        environment.define(ReadFunction.NAME, ReadFunction())
        environment.define(ToNumberFunction.NAME, ToNumberFunction())
        environment.define(ToStringFunction.NAME, ToStringFunction())
    }

    fun interpret(stmts: List<Statement>) {
        stmts.forEach { interpret(it) }
    }

    fun interpret(stmt: Statement) = try {
        stmt.execute()
    } catch (e: RuntimeError) {
        errorHandler.runtimeError(e)
    }

    fun execute(stmts: Iterable<Statement>, env: Environment) {
        stmts.execute(env)
    }

    override fun visit(stmt: Statement.Expression) {
        stmt.expression.evaluate()
    }

    override fun visit(stmt: Statement.Function) =
            environment.define(stmt.name, LoxFunction(stmt.function, environment, stmt.name.lexeme))

    override fun visit(stmt: Statement.If) {
        when {
            stmt.condition.evaluate().isTruthy -> stmt.thenStmt.execute()
            stmt.elseStmt != null -> stmt.elseStmt.execute()
        }
    }

    override fun visit(stmt: Statement.Return) {
        val value = if (stmt.value == null) null else stmt.value.evaluate()
        throw ReturnException(value)
    }

    override fun visit(stmt: Statement.Variable) {
        environment.define(stmt.name, stmt.initializer.evaluate())
    }

    override fun visit(stmt: Statement.While) = try {
        while (stmt.condition.evaluate().isTruthy) {
            try {
                stmt.body.execute()
            } catch (e: ContinueException) { }
        }
    } catch (e: BreakException) { }

    override fun visit(stmt: Statement.For) = try {
        stmt.init?.execute()
        while (stmt.condition.evaluate().isTruthy) {
            try {
                stmt.body.execute()
            } catch (e: ContinueException) { }
            stmt.increment?.execute()
        }
    } catch (e: BreakException) { }

    override fun visit(stmt: Statement.Block) {
        stmt.statements.execute(Environment(environment))
    }

    override fun visit(stmt: Statement.Break) = throw BreakException()

    override fun visit(stmt: Statement.Continue) = throw ContinueException()

    override fun visit(expr: Expression.Assignment): Any? {
        val value = expr.value.evaluate()
        environment.assign(expr.name, value)
        return value
    }

    override fun visit(expr: Expression.Binary): Any? {
        val left = expr.left.evaluate()
        val right = expr.right.evaluate()

        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }

            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                val r = right as Double
                if (r == 0.0)
                    throw RuntimeError(expr.operator, "Division by zero.")

                (left as Double) / r
            }

            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }

            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }

            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }

            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }

            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }

            TokenType.BANG_EQUAL -> left != right
            TokenType.EQUAL_EQUAL -> left == right

            TokenType.PLUS -> when {
                left is Double && right is Double -> left + right
                left is String -> left + right.stringify()

                else -> throw RuntimeError(expr.operator, "Operands must be two numbers or strings.")
            }

            TokenType.COMMA -> right

            else -> throw RuntimeError(expr.operator, "Unsupported operator.")
        }
    }

    override fun visit(expr: Expression.Call): Any? {
        val callee = expr.callee.evaluate()

        val arguments = expr.arguments.evaluate()

        if (callee !is LoxCallable)
            throw RuntimeError(expr.paren, "Can only call functions and classes.")

        if (arguments.size != callee.arity)
            throw RuntimeError(expr.paren, "Expected ${callee.arity} arguments, got ${arguments.size}.")

        return callee(this, arguments)
    }

    override fun visit(expr: Expression.Grouping) = expr.expression.evaluate()

    override fun visit(expr: Expression.Literal) = expr.value

    override fun visit(expr: Expression.Logical): Any? {
        val left = expr.left.evaluate()

        if (expr.operator.type == TokenType.OR) {
            if (left.isTruthy)
                return left
        } else if (!left.isTruthy) {
            return left
        }

        return expr.right.evaluate()
    }

    override fun visit(expr: Expression.Unary): Any? {
        val right = expr.right.evaluate()

        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, right)
                -(right as Double)
            }
            TokenType.BANG -> !right.isTruthy
            else -> null
        }
    }

    override fun visit(expr: Expression.Variable) = environment[expr.name]

    override fun visit(expr: Expression.Conditional) =
            if (expr.expression.evaluate().isTruthy)
                expr.truthy.evaluate()
            else
                expr.falsey.evaluate()

    override fun visit(expr: Expression.Function) = LoxFunction(expr, environment)

    private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
        if (operands.all { it is Double })
            return

        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun Statement.execute() = accept(this@Interpreter)

    private fun Iterable<Statement>.execute() = forEach { it.execute() }

    @JvmName("stmtsExecuteWithEnvExt")
    private fun Iterable<Statement>.execute(env: Environment) {
        val previousEnv = environment

        try {
            environment = env
            this.execute()
        } finally {
            environment = previousEnv
        }
    }

    private fun Expression.evaluate() = accept(this@Interpreter)

    private fun Iterable<Expression>.evaluate() = map { it.evaluate() }
}
