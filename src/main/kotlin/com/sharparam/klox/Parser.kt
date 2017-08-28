package com.sharparam.klox

import com.sharparam.klox.util.logger

class Parser(private val tokens: List<Token>, private val errorHandler: ErrorHandler) {
    private val log by logger()

    private val isAtEnd get() = peek().type == TokenType.EOF

    private var current = 0

    private var loopDepth = 0

    fun parse(): List<Statement> {
        val statements = ArrayList<Statement>()

        while (!isAtEnd) {
            val stmt = declaration()
            if (stmt != null)
                statements.add(stmt)
        }

        return statements
    }

    private fun declaration() = try {
        when {
            check(TokenType.FUN) && checkNext(TokenType.IDENTIFIER) -> {
                consume(TokenType.FUN, "Expected 'fun' to declare function.")
                function("function")
            }
            match(TokenType.VAR) -> varDeclaration()
            else -> statement()
        }
    } catch (e: ParseError) {
        synchronize()
        null
    }

    private fun function(kind: String): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expected $kind name.")

        return Statement.Function(name, functionBody(kind))
    }

    private fun functionBody(kind: String): Expression.Function {
        consume(TokenType.LEFT_PAREN, "Expected '(' after $kind name.")

        val parameters = ArrayList<Token>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                parameters.add(consume(TokenType.IDENTIFIER, "Expected parameter name."))
            } while (match(TokenType.COMMA))
        }

        if (parameters.size >= MAX_ARGUMENT_COUNT)
            error(peek(), "Cannot have more than $MAX_ARGUMENT_COUNT parameters.")

        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameter list.")
        consume(TokenType.LEFT_BRACE, "Expected '{' before $kind body.")

        return Expression.Function(parameters, block())
    }

    private fun varDeclaration(): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expected variable name.")

        val initializer = if (match(TokenType.EQUAL)) expression() else Expression.Literal(null)

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.")

        return Statement.Variable(name, initializer)
    }

    private fun statement() = when {
        match(TokenType.FOR) -> forStatement()
        match(TokenType.IF) -> ifStatement()
        match(TokenType.PRINT) -> printStatement()
        match(TokenType.RETURN) -> returnStatement()
        match(TokenType.WHILE) -> whileStatement()
        match(TokenType.LEFT_BRACE) -> Statement.Block(block())
        match(TokenType.BREAK) -> breakStatement()
        match(TokenType.CONTINUE) -> continueStatement()
        else -> expressionStatement()
    }

    private fun forStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'.")

        val init = when {
            match(TokenType.SEMICOLON) -> null
            match(TokenType.VAR) -> varDeclaration()
            else -> expressionStatement()
        }

        val cond = if (check(TokenType.SEMICOLON)) Expression.Literal(true) else expression()
        consume(TokenType.SEMICOLON, "Expected ';' after for condition.")

        val incr = if (check(TokenType.RIGHT_PAREN)) null else Statement.Expression(expression())
        consume(TokenType.RIGHT_PAREN, "Expected ')' after for clause.")

        try {
            loopDepth++
            return Statement.For(init, cond, incr, statement())
        } finally {
            loopDepth--
        }
    }

    private fun ifStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'.")

        val condition = expression()

        consume(TokenType.RIGHT_PAREN, "Expected ')' after condition.")

        val thenStmt = statement()
        val elseStmt = if (match(TokenType.ELSE)) statement() else null

        return Statement.If(condition, thenStmt, elseStmt)
    }

    private fun printStatement(): Statement {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after value.")
        return Statement.Print(value)
    }

    private fun returnStatement(): Statement {
        val keyword = previous()

        val value = if (check(TokenType.SEMICOLON)) null else expression()
        consume(TokenType.SEMICOLON, "Expected ';' after return statement.")
        return Statement.Return(keyword, value)
    }

    private fun whileStatement(): Statement {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after condition.")

        try {
            loopDepth++
            val body = statement()
            return Statement.While(condition, body)
        } finally {
            loopDepth--
        }
    }

    private fun block(): Iterable<Statement> {
        val statements = ArrayList<Statement>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd) {
            val stmt = declaration()
            if (stmt != null)
                statements.add(stmt)
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' to end block.")

        return statements
    }

    private fun breakStatement() = flowStatement(Statement::Break, "break")

    private fun continueStatement() = flowStatement(Statement::Continue, "continue")

    private fun <T: Statement> flowStatement(ctor: () -> T, type: String): T {
        if (loopDepth == 0)
            error(previous(), "'$type' can only be used inside loops.")

        consume(TokenType.SEMICOLON, "Expected ';' after '$type'.")
        return ctor()
    }

    private fun expressionStatement(): Statement {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after expression.")
        return Statement.Expression(expr)
    }

    private fun expression() = comma()

    private fun comma() = binary(Expression::Binary, this::assignment, TokenType.COMMA)

    private fun assignment(): Expression {
        val expr = conditional()

        if (match(TokenType.EQUAL, TokenType.PLUS_EQUAL, TokenType.MINUS_EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expression.Variable) {
                val name = expr.name
                return Expression.Assignment(name, when (equals.type) {
                    TokenType.PLUS_EQUAL -> Expression.Binary(
                            expr,
                            Token(TokenType.PLUS, "+", null, equals.line, equals.column),
                            value
                    )
                    TokenType.MINUS_EQUAL -> Expression.Binary(
                            expr,
                            Token(TokenType.MINUS, "-", null, equals.line, equals.column),
                            value
                    )
                    else -> value
                })
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    private fun conditional(): Expression {
        var expr = or()

        if (match(TokenType.QUESTION)) {
            val truthy = expression()
            consume(TokenType.COLON, "Expected ':' in conditional expression.")
            val falsey = conditional()
            expr = Expression.Conditional(expr, truthy, falsey)
        }

        return expr
    }

    private fun or() = binary(Expression::Logical, this::and, TokenType.OR)

    private fun and() = binary(Expression::Logical, this::equality, TokenType.AND)

    private fun equality() = binary(Expression::Binary, this::comparison, TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)

    private fun comparison() = binary(Expression::Binary, this::addition, TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)

    private fun addition() = binary(Expression::Binary, this::multiplication, TokenType.MINUS, TokenType.PLUS)

    private fun multiplication() = binary(Expression::Binary, this::unary, TokenType.SLASH, TokenType.STAR)

    private fun unary(): Expression {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val op = previous()
            val right = unary()
            return Expression.Unary(op, right)
        }

        return call()
    }

    private fun call(): Expression {
        var expr = primary()

        while (match(TokenType.LEFT_PAREN))
            expr = finishCall(expr)

        return expr
    }

    private fun finishCall(callee: Expression): Expression {
        val arguments = ArrayList<Expression>()

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments.add(assignment())
            } while (match(TokenType.COMMA))
        }

        if (arguments.size >= MAX_ARGUMENT_COUNT)
            error(peek(), "Function call cannot have more than $MAX_ARGUMENT_COUNT arguments.")

        val paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments.")

        return Expression.Call(callee, paren, arguments)
    }

    private fun primary() = when {
        match(TokenType.FALSE) -> Expression.Literal(false)
        match(TokenType.TRUE) -> Expression.Literal(true)
        match(TokenType.NIL) -> Expression.Literal(null)
        match(TokenType.NUMBER, TokenType.STRING) -> Expression.Literal(previous().literal)
        match(TokenType.IDENTIFIER) -> Expression.Variable(previous())
        match(TokenType.FUN) -> functionBody("lambda")

        match(TokenType.LEFT_PAREN) -> {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.")
            Expression.Grouping(expr)
        }

        match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL) -> {
            error(previous(), "Missing left-hand operand.")
            equality()
            Expression.Literal(null)
        }

        match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL) -> {
            error(previous(), "Missing left-hand operand.")
            comparison()
            Expression.Literal(null)
        }

        match(TokenType.PLUS) -> {
            error(previous(), "Missing left-hand operand.")
            addition()
            Expression.Literal(null)
        }

        match(TokenType.STAR, TokenType.SLASH) -> {
            error(previous(), "Missing left-hand operand.")
            multiplication()
            Expression.Literal(null)
        }

        else -> throw error(peek(), "Expected expression.")
    }

    private fun <T : Expression> binary(ctor: (Expression, Token, Expression) -> T, next: () -> Expression, vararg tokens: TokenType): Expression {
        var expr = next()

        while (match(*tokens)) {
            val op = previous()
            val right = next()
            expr = ctor(expr, op, right)
        }

        return expr
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type))
            return advance()

        throw error(peek(), message)
    }

    private fun match(vararg types: TokenType): Boolean {
        if (types.any { check(it) }) {
            advance()
            return true
        }

        return false
    }

    private fun check(type: TokenType) = if (isAtEnd) false else peek().type == type

    private fun checkNext(type: TokenType) = when {
        isAtEnd -> false
        tokens[current + 1].type == TokenType.EOF -> false
        else -> tokens[current + 1].type == type
    }

    private fun advance(): Token {
        if (!isAtEnd)
            current++

        return previous()
    }

    private fun peek() = tokens[current]

    private fun previous() = tokens[current - 1]

    private fun synchronize() {
        log.debug("PARSER PANIC! Synchronizing...")

        advance()

        while (!isAtEnd) {
            if (previous().type == TokenType.SEMICOLON)
                return

            when (peek().type) {
                TokenType.CLASS, TokenType.FUN, TokenType.VAR, TokenType.FOR, TokenType.IF,
                TokenType.WHILE, TokenType.PRINT, TokenType.RETURN -> return
                else -> {
                }
            }

            advance()
        }
    }

    private fun error(token: Token, message: String): ParseError {
        errorHandler.parseError(token, message)
        return ParseError()
    }

    companion object {
        private const val MAX_ARGUMENT_COUNT = 8
    }
}
