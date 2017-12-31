package com.sharparam.klox

import com.sharparam.klox.util.logger
import java.util.*

class Resolver(private val interpreter: Interpreter, private val errorHandler: ErrorHandler) : Expression.Visitor<Unit>, Statement.Visitor<Unit> {
    private val log by logger()

    private val scopes: Stack<MutableMap<String, Boolean>> = Stack()

    private var currentFunction = FunctionType.NONE

    private var currentClassType = ClassType.NONE

    fun resolve(statements: Iterable<Statement>) = statements.resolve()

    override fun visit(expr: Expression.Assignment) {
        expr.value.resolve()
        expr.resolveLocal(expr.name)
    }

    override fun visit(expr: Expression.Binary) {
        expr.left.resolve()
        expr.right.resolve()
    }

    override fun visit(expr: Expression.Call) {
        expr.callee.resolve()
        expr.arguments.resolve()
    }

    override fun visit(expr: Expression.Get) = expr.target.resolve()

    override fun visit(expr: Expression.Set) {
        expr.value.resolve()
        expr.target.resolve()
    }

    override fun visit(expr: Expression.Grouping) = expr.expression.resolve()

    override fun visit(expr: Expression.Literal) {}

    override fun visit(expr: Expression.Logical) {
        expr.left.resolve()
        expr.right.resolve()
    }

    override fun visit(expr: Expression.This) {
        if (currentClassType == ClassType.NONE)
            errorHandler.resolveError(expr.keyword, "Cannot use 'this' outside of class.")
        else
            expr.resolveLocal(expr.keyword)
    }

    override fun visit(expr: Expression.Unary) = expr.right.resolve()

    override fun visit(expr: Expression.Variable) {
        if (!scopes.empty() && scopes.peek()[expr.name.lexeme] == false)
            errorHandler.resolveError(expr.name, "Cannot read local variable in its own initializer.")

        expr.resolveLocal(expr.name)
    }

    override fun visit(expr: Expression.Function) {
        expr.resolveFunction(FunctionType.FUNCTION)
    }

    override fun visit(expr: Expression.Conditional) {
        expr.expression.resolve()
        expr.truthy.resolve()
        expr.falsey.resolve()
    }

    override fun visit(stmt: Statement.Expression) = stmt.expression.resolve()

    override fun visit(stmt: Statement.Class) {
        stmt.name.declare()
        stmt.name.define()

        log.trace("--- CLASS {} ---", stmt.name.lexeme)

        val enclosingClassType = currentClassType
        currentClassType = ClassType.CLASS

        scope {
            it["this"] = true
            stmt.methods.forEach {
                it.function.resolveFunction(
                        if (it.name.lexeme == "init") FunctionType.INITIALIZER else FunctionType.METHOD
                )
            }
        }

        currentClassType = enclosingClassType
        log.trace("--- END CLASS {} ---", stmt.name.lexeme)
    }

    override fun visit(stmt: Statement.Function) {
        stmt.name.declare()
        stmt.name.define()
        stmt.function.resolveFunction(FunctionType.FUNCTION)
    }

    override fun visit(stmt: Statement.If) {
        stmt.condition.resolve()
        stmt.thenStmt.resolve()
        stmt.elseStmt?.resolve()
    }

    override fun visit(stmt: Statement.Variable) {
        stmt.name.declare()
        stmt.initializer.resolve()
        stmt.name.define()
    }

    override fun visit(stmt: Statement.Return) {
        if (currentFunction == FunctionType.NONE)
            errorHandler.resolveError(stmt.keyword, "Cannot return from top-level code.")
        else if (currentFunction == FunctionType.INITIALIZER)
            errorHandler.resolveError(stmt.keyword, "Cannot return from initializer.")

        stmt.value?.resolve()
    }

    override fun visit(stmt: Statement.While) {
        stmt.condition.resolve()
        stmt.body.resolve()
    }

    override fun visit(stmt: Statement.For) {
        stmt.init?.resolve()
        stmt.condition.resolve()
        stmt.increment?.resolve()
        stmt.body.resolve()
    }

    override fun visit(stmt: Statement.Block) = scope { stmt.statements.resolve() }

    override fun visit(stmt: Statement.Break) {}

    override fun visit(stmt: Statement.Continue) {}

    private fun beginScope() {
        log.trace("--- BEGIN SCOPE ({}) ---", scopes.size + 1)
        scopes.push(HashMap())
    }

    private fun endScope() {
        scopes.pop()
        log.trace("--- END SCOPE ({}) ---", scopes.size + 1)
    }

    private fun scope(block: (MutableMap<String, Boolean>) -> Unit) {
        beginScope()
        scopes.peek().apply(block)
        endScope()
    }

    private fun Token.declare() {
        if (scopes.empty())
            return

        val scope = scopes.peek()

        log.trace("Declaring {} at scope level {}", lexeme, scopes.size)

        if (scope.contains(lexeme))
            errorHandler.resolveError(this, "Variable '$lexeme' already declared in this scope.")

        scope.put(lexeme, false)
    }

    private fun Token.define() {
        if (scopes.empty())
            return

        log.trace("Defining {} at scope level {}", lexeme, scopes.size)
        scopes.peek().put(lexeme, true)
    }

    @Suppress("unused")
    @JvmName("resolveStatements")
    private fun Iterable<Statement>.resolve() = forEach { it.resolve() }

    private fun Statement.resolve() = accept(this@Resolver)

    @Suppress("unused")
    @JvmName("resolveExpressions")
    private fun Iterable<Expression>.resolve() = forEach { it.resolve() }

    private fun Expression.resolve() = accept(this@Resolver)

    private fun Expression.resolveLocal(name: Token) {
        for (i in scopes.size - 1 downTo 0) {
            if (scopes[i].contains(name.lexeme)) {
                interpreter.resolve(this, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun Expression.Function.resolveFunction(type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type

        scope {
            parameters.forEach {
                it.declare()
                it.define()
            }

            body.resolve()
        }

        currentFunction = enclosingFunction
    }

    private enum class FunctionType {
        NONE,
        FUNCTION,
        INITIALIZER,
        METHOD
    }

    private enum class ClassType {
        NONE,
        CLASS
    }
}
