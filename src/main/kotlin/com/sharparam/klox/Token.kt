package com.sharparam.klox

class Token(val type: TokenType, val lexeme: String, val literal: Any?, val line: Int, val column: Int) {
    override fun toString() = "$type $lexeme $literal"
}
