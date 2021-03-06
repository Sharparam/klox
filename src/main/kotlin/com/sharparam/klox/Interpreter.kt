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
import com.sharparam.klox.util.logger
import com.sharparam.klox.util.stringify
import com.sharparam.klox.util.toMap

class Interpreter(private val errorHandler: ErrorHandler) : Expression.Visitor<Any?>, Statement.Visitor<Unit> {
    internal val globals = Environment()

    private val log by logger()

    private val locals = HashMap<Expression, Int>()

    private var environment = globals

    private val Any?.isTruthy: Boolean get() = when (this) {
        null -> false
        is Boolean -> this
        else -> true
    }

    init {
        log.debug("Initializing")
        globals.define(TypeFunction.NAME, TypeFunction())
        globals.define(ClockFunction.NAME, ClockFunction())
        globals.define(PrintFunction.NAME, PrintFunction())
        globals.define(ReadFunction.NAME, ReadFunction())
        globals.define(ToNumberFunction.NAME, ToNumberFunction())
        globals.define(ToStringFunction.NAME, ToStringFunction())
        globals.define(SleepFunction.NAME, SleepFunction())
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

    fun resolve(expr: Expression, depth: Int) {
        locals.put(expr, depth)
    }

    override fun visit(stmt: Statement.Expression) {
        stmt.expression.evaluate()
    }

    override fun visit(stmt: Statement.Class) {
        var superclass: Any? = null

        if (stmt.superclass != null) {
            superclass = stmt.superclass.evaluate()

            if (superclass !is LoxClass) {
                throw RuntimeError(stmt.superclass.name, "Superclass must be a class.")
            }
        }

        environment.define(stmt.name, null)

        if (superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }

        log.trace("Declaring class {} with {} methods", stmt.name.lexeme, stmt.methods.count())
        val methods = stmt.methods.toMap({ it.name.lexeme }) {
            LoxFunction(it.function, environment, it.name.lexeme, it.name.lexeme == "init")
        }

        val cls = LoxClass(stmt.name.lexeme, superclass as LoxClass?, methods)

        if (superclass != null)
            environment = environment.parent!!

        environment.assign(stmt.name, cls)
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
        log.trace("Returning {}", value)
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
        val dist = locals[expr]

        when (dist) {
            null -> globals.assign(expr.name, value)
            else -> environment.assignAt(dist, expr.name, value)
        }

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

    override fun visit(expr: Expression.Get): Any? {
        val target = expr.target.evaluate()

        if (target is LoxInstance) {
            return target[expr.name]
        }

        throw RuntimeError(expr.name, "Only class instances have properties.")
    }

    override fun visit(expr: Expression.Set): Any? {
        val target = expr.target.evaluate() as? LoxInstance ?:
                throw RuntimeError(expr.name, "Only class instances have fields.")

        val value = expr.value.evaluate()
        target[expr.name] = value
        return value
    }

    override fun visit(expr: Expression.This) = expr.lookUpVariable(expr.keyword)

    override fun visit(expr: Expression.Super): Any? {
        val distance = locals[expr] ?: throw RuntimeError(expr.keyword, "Failed to get variable distance for 'super'.")
        val superclass = environment.getAt(distance, "super") as LoxClass
        val instance = environment.getAt(distance - 1, "this") as LoxInstance

        return superclass.getMethod(expr.method.lexeme, instance)
                ?: throw RuntimeError(expr.method, "Undefined property '${expr.method.lexeme}'.")
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

    override fun visit(expr: Expression.Variable) = expr.lookUpVariable(expr.name)

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

    private fun Expression.lookUpVariable(name: Token): Any? {
        val dist = locals[this]
        return when (dist) {
            null -> globals[name]
            else -> environment.getAt(dist, name)
        }
    }

    private fun Statement.execute() = accept(this@Interpreter)

    private fun Iterable<Statement>.execute() = forEach { it.execute() }

    @Suppress("unused")
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
