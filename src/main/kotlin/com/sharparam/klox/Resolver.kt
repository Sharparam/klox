package com.sharparam.klox

import java.util.*

class Resolver(private val interpreter: Interpreter, private val errorHandler: ErrorHandler) : Expression.Visitor<Unit>, Statement.Visitor<Unit> {
    private val scopes: Stack<MutableMap<String, Boolean>> = Stack()

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(expr: Expression.Conditional) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visit(stmt: Statement.Expression) = stmt.resolve()

    override fun visit(stmt: Statement.Function) {
        stmt.name.declare()
        stmt.name.define()
        stmt.resolveFunction(FunctionType.FUNCTION)
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
        if (!scopes.empty())
            scopes.peek().put(lexeme, false)
    }

    private fun Token.define() {
        if (!scopes.empty())
            scopes.peek().put(lexeme, true)
    }

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

    private fun Statement.Function.resolveFunction(type: FunctionType) {
        beginScope()

        function.parameters.forEach {
            it.declare()
            it.define()
        }

        function.body.resolve()
        endScope()
    }
}
