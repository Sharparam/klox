package com.sharparam.klox

class Interpreter(private val errorHandler: ErrorHandler) : Expression.Visitor<Any?>, Statement.Visitor<Unit> {
    private val environment = Environment()

    private val Any?.isTruthy: Boolean get() = when (this) {
        null -> false
        is Boolean -> this
        else -> true
    }

    fun interpret(stmts: List<Statement>) = try {
        stmts.forEach { it.execute() }
    } catch (e: RuntimeError) {
        errorHandler.runtimeError(e)
    }

    override fun visit(stmt: Statement.Expression): Unit {
        stmt.expression.evaluate()
    }

    override fun visit(stmt: Statement.Print) = println(stmt.expression.evaluate().stringify())

    override fun visit(stmt: Statement.Variable) {
        environment[stmt.name] = stmt.initializer.evaluate()
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

            else -> throw RuntimeError(expr.operator, "Unsupported operator.")
        }
    }

    override fun visit(expr: Expression.Grouping) = expr.expression.evaluate()

    override fun visit(expr: Expression.Literal) = expr.value

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
