package com.sharparam.klox

class Token(val type: TokenType, val lexeme: String, val literal: Any?, val line: Int, val column: Int) {
    override fun toString() = "$type $lexeme $literal"

    override fun equals(other: Any?) =
            other is Token && type == other.type && lexeme == other.lexeme && literal == other.literal &&
                    line == other.line && column == other.column

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + lexeme.hashCode()
        result = 31 * result + (literal?.hashCode() ?: 0)
        result = 31 * result + line
        result = 31 * result + column
        return result
    }
}
