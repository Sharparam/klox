package com.sharparam.klox

class Interpreter(private val errorHandler: ErrorHandler) : Expression.Visitor<Any?>, Statement.Visitor<Unit> {
    private var environment = Environment()

    private val Any?.isTruthy: Boolean get() = when (this) {
        null -> false
        is Boolean -> this
        else -> true
    }

    fun interpret(stmts: List<Statement>) {
        stmts.forEach { interpret(it) }
    }

    fun interpret(stmt: Statement) = try {
        stmt.execute()
    } catch (e: RuntimeError) {
        errorHandler.runtimeError(e)
    }

    override fun visit(stmt: Statement.Expression) {
        stmt.expression.evaluate()
    }

    override fun visit(stmt: Statement.If) {
        when {
            stmt.condition.evaluate().isTruthy -> stmt.thenStmt.execute()
            stmt.elseStmt != null -> stmt.elseStmt.execute()
        }
    }

    override fun visit(stmt: Statement.Print) = println(stmt.expression.evaluate().stringify())

    override fun visit(stmt: Statement.Variable) {
        environment.define(stmt.name, stmt.initializer.evaluate())
    }

    override fun visit(stmt: Statement.While) {
        while (stmt.condition.evaluate().isTruthy)
            stmt.body.execute()
    }

    override fun visit(stmt: Statement.Block) {
        stmt.statements.execute(Environment(environment))
    }

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

    private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
        if (operands.all { it is Double })
            return

        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun Statement.execute() = this.accept(this@Interpreter)

    private fun List<Statement>.execute() = forEach { it.execute() }

    private fun List<Statement>.execute(env: Environment) {
        val previousEnv = environment

        try {
            environment = env
            this.execute()
        } finally {
            environment = previousEnv
        }
    }

    private fun Expression.evaluate() = this.accept(this@Interpreter)

    private fun Any?.stringify(): String = when (this) {
        null -> "nil"

        is Double -> {
            val text = this.toString()
            if (text.endsWith(".0"))
                text.substring(0, text.length - 2)
            else
                text
        }

        else -> this.toString()
    }
}
