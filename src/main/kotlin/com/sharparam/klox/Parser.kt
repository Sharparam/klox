package com.sharparam.klox

class Parser(private val tokens: List<Token>, private val errorHandler: ErrorHandler) {
    private val isAtEnd get() = peek().type == TokenType.EOF

    private var current: Int = 0

    fun parse() = try {
        expression()
    } catch (e: ParseError) {
        null
    }

    private fun expression() = equality()

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

        match(TokenType.LEFT_PAREN) -> {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.")
            Expression.Grouping(expr)
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
