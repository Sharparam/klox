package com.sharparam.klox

class Scanner(private val source: String, private val errorHandler: ErrorHandler) {
    private val KEYWORDS = hashMapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
    )

    private val tokens: MutableList<Token> = ArrayList()

    private var start = 0
    private var current = 0
    private var line = 1

    private val isAtEnd: Boolean
        get() = current >= source.length

    fun scanTokens(): List<Token> {
        while (!isAtEnd) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()
        when(c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '?' -> addToken(TokenType.QUESTION)
            ':' -> addToken(TokenType.COLON)

            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)

            '/' -> {
                when {
                    match('/') -> {
                        while (peek() != '\n' && !isAtEnd)
                            advance()
                    }
                    match('*') -> comment()
                    else -> addToken(TokenType.SLASH)
                }
            }

            '"' -> string()

            in '0'..'9' -> number()

            in 'a'..'z', in 'A'..'Z', '_' -> identifier()

            ' ', '\r', '\t' -> { }

            '\n' -> line++

            else -> errorHandler.scanError(line, "Unexpected character: $c")
        }
    }

    private fun advance(): Char = source[current++]

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd || source[current] != expected)
            return false

        current++
        return true
    }

    private fun peek() = if (isAtEnd) '\u0000' else source[current]

    private fun peekNext() = if (current + 1 >= source.length) '\u0000' else source[current + 1]

    private fun string() {
        while (peek() != '"' && !isAtEnd) {
            if (peek() == '\n')
                line++
            advance()
        }

        if (isAtEnd) {
            errorHandler.scanError(line, "Unterminated string")
            return
        }

        advance()

        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun number() {
        while (peek() in '0'..'9')
            advance()

        if (peek() == '.' && peekNext() in '0'..'9') {
            advance()

            while (peek() in '0'..'9')
                advance()
        }

        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun identifier() {
        while (peek().isAlphaNumeric)
            advance()

        val text = source.substring(start, current)
        val type = KEYWORDS[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun comment() {
        while (peek() != '*' && peekNext() != '/' && !isAtEnd) {
            if (peek() == '\n')
                line++
            advance()
        }

        if (current + 1 >= source.length) {
            errorHandler.scanError(line, "Unterminated comment")
            return
        }

        // Consume '*'
        advance()

        // Consume '/'
        advance()
    }
}
