package com.sharparam.klox

import java.util.*

class Resolver(private val interpreter: Interpreter, private val errorHandler: ErrorHandler) : Expression.Visitor<Unit>, Statement.Visitor<Unit> {
    private val scopes: Stack<MutableMap<String, Boolean>> = Stack()

    private var currentFunction = FunctionType.NONE

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

    override fun visit(expr: Expression.Grouping) = expr.expression.resolve()

    override fun visit(expr: Expression.Literal) {}

    override fun visit(expr: Expression.Logical) {
        expr.left.resolve()
        expr.right.resolve()
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

    override fun visit(stmt: Statement.Expression) = stmt.resolve()

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

    override fun visit(stmt: Statement.Block) {
        beginScope()
        stmt.statements.resolve()
        endScope()
    }

    override fun visit(stmt: Statement.Break) {}

    override fun visit(stmt: Statement.Continue) {}

    private fun beginScope() = scopes.push(HashMap())

    private fun endScope() = scopes.pop()

    private fun Token.declare() {
        if (scopes.empty())
            return

        val scope = scopes.peek()

        if (scope.contains(lexeme))
            errorHandler.resolveError(this, "Variable '$lexeme' already declared in this scope.")

        scope.put(lexeme, false)
    }

    private fun Token.define() {
        if (!scopes.empty())
            scopes.peek().put(lexeme, true)
    }

    @JvmName("resolveStatements")
    private fun Iterable<Statement>.resolve() = forEach { it.resolve() }

    private fun Statement.resolve() = accept(this@Resolver)

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

        beginScope()

        parameters.forEach {
            it.declare()
            it.define()
        }

        body.resolve()
        endScope()

        currentFunction = enclosingFunction
    }

    private enum class FunctionType {
        NONE,
        FUNCTION
    }
}
