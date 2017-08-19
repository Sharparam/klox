package com.sharparam.klox

class Parser(private val tokens: List<Token>, private val errorHandler: ErrorHandler) {
    private val log by logger()

    private val isAtEnd get() = peek().type == TokenType.EOF

    private var current: Int = 0

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
            match(TokenType.VAR) -> varDeclaration()
            else -> statement()
        }
    } catch (e: ParseError) {
        synchronize()
        null
    }

    private fun varDeclaration(): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expected variable name.")

        val initializer = if (match(TokenType.EQUAL)) expression() else Expression.Literal(null)

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.")

        return Statement.Variable(name, initializer)
    }

    private fun statement() = when {
        match(TokenType.PRINT) -> printStatement()
        else -> expressionStatement()
    }

    private fun printStatement(): Statement {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after value.")
        return Statement.Print(value)
    }

    private fun expressionStatement(): Statement {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expected ';' after expression.")
        return Statement.Expression(expr)
    }

    private fun expression() = comma()

    private fun comma() = binary(this::conditional, TokenType.COMMA)

    private fun conditional(): Expression {
        var expr = equality()

        if (match(TokenType.QUESTION)) {
            val truthy = expression()
            consume(TokenType.COLON, "Expected ':' in conditional expression.")
            val falsey = conditional()
            expr = Expression.Conditional(expr, truthy, falsey)
        }

        return expr
    }

    private fun equality() = binary(this::comparison, TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)

    private fun comparison() = binary(this::addition, TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)

    private fun addition() = binary(this::multiplication, TokenType.MINUS, TokenType.PLUS)

    private fun multiplication() = binary(this::unary, TokenType.SLASH, TokenType.STAR)

    private fun unary(): Expression {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val op = previous()
            val right = unary()
            return Expression.Unary(op, right)
        }

        return primary()
    }

    private fun primary() = when {
        match(TokenType.FALSE) -> Expression.Literal(false)
        match(TokenType.TRUE) -> Expression.Literal(true)
        match(TokenType.NIL) -> Expression.Literal(null)
        match(TokenType.NUMBER, TokenType.STRING) -> Expression.Literal(previous().literal)
        match(TokenType.IDENTIFIER) -> Expression.Variable(previous())

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

    private fun binary(next: () -> Expression, vararg tokens: TokenType): Expression {
        var expr = next()

        while (match(*tokens)) {
            val op = previous()
            val right = next()
            expr = Expression.Binary(expr, op, right)
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
                else -> { }
            }

            advance()
        }
    }

    private fun error(token: Token, message: String): ParseError {
        errorHandler.parseError(token, message)
        return ParseError()
    }
}
