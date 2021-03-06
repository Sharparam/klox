/*
 * Copyright (c) 2017 by Adam Hellberg.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.sharparam.klox

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.*
import kotlin.test.assertEquals

class ScannerTests {
    @Test fun shouldScanEofOnEmptyString() {
        val scanner = Scanner("", mock(ErrorHandler::class.java))
        val tokens = scanner.scanTokens()
        assertEquals(listOf(TokenType.EOF), tokens.map { it.type })
    }

    @ParameterizedTest
    @MethodSource("generateSimpleTokens")
    fun shouldScanSimpleToken(input: String, expectedToken: TokenType) {
        val scanner = Scanner(input, mock(ErrorHandler::class.java))
        val tokens = scanner.scanTokens()
        assertEquals(listOf(expectedToken, TokenType.EOF), tokens.map { it.type })
    }

    @ParameterizedTest
    @ValueSource(strings = arrayOf(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "42", "9001", "42.9001", "9001.42", "123.000023241",
            "040242025.328032"
    ))
    fun shouldScanNumber(input: String) {
        val scanner = Scanner(input, mock(ErrorHandler::class.java))
        val tokens = scanner.scanTokens()
        assertEquals(listOf(
                Token(TokenType.NUMBER, input, input.toDouble(), 1, input.length),
                Token(TokenType.EOF, "", null, 1, input.length)
        ), tokens)
    }

    @ParameterizedTest
    @ValueSource(strings = arrayOf("foo", "bar", "baz"))
    fun shouldScanIdentifier(input: String) {
        val scanner = Scanner(input, mock(ErrorHandler::class.java))
        val tokens = scanner.scanTokens()
        assertEquals(listOf(
                Token(TokenType.IDENTIFIER, input, null, 1, input.length),
                Token(TokenType.EOF, "", null, 1, input.length)
        ), tokens)
    }

    @ParameterizedTest
    @ValueSource(strings = arrayOf("foo", "bar", "baz", "c0mplëx_str%ng!!+++"))
    fun shouldScanString(input: String) {
        val lexeme = """"$input""""
        val scanner = Scanner(lexeme, mock(ErrorHandler::class.java))
        val tokens = scanner.scanTokens()
        assertEquals(listOf(
                Token(TokenType.STRING, lexeme, input, 1, lexeme.length),
                Token(TokenType.EOF, "", null, 1, lexeme.length)
        ), tokens)
    }

    @Test fun shouldIgnoreLineComment() {
        val scanner = Scanner("// this is a comment", mock(ErrorHandler::class.java))
        val tokens = scanner.scanTokens()
        assertEquals(listOf(TokenType.EOF), tokens.map { it.type })
    }

    @Test fun shouldIgnoreBlockCommentAtLineStart() {
        val scanner = Scanner("/* comment */+", mock(ErrorHandler::class.java))
        val tokens = scanner.scanTokens()
        assertEquals(listOf(TokenType.PLUS, TokenType.EOF), tokens.map { it.type })
    }

    @Test fun shouldIgnoreBlockCommentAtLineEnd() {
        val scanner = Scanner("+/* comment */", mock(ErrorHandler::class.java))
        val tokens = scanner.scanTokens()
        assertEquals(listOf(TokenType.PLUS, TokenType.EOF), tokens.map { it.type })
    }

    @Test fun shouldIgnoreBlockCommentInMiddleOfLine() {
        val scanner = Scanner("+/* comment*/+", mock(ErrorHandler::class.java))
        val tokens = scanner.scanTokens()
        assertEquals(listOf(TokenType.PLUS, TokenType.PLUS, TokenType.EOF), tokens.map { it.type })
    }

    @ParameterizedTest
    @ValueSource(strings = arrayOf("^", "€", "#", "@", "$", "\\"))
    fun shouldErrorOnUnexpectedCharacter(input: String) {
        val errorHandler = mock(ErrorHandler::class.java)
        val scanner = Scanner(input, errorHandler)
        scanner.scanTokens()
        verify(errorHandler).scanError(eq(1), eq(1), contains(input))
    }

    companion object {
        private val SIMPLE_TOKENS = arrayOf(
                Arguments.of("(", TokenType.LEFT_PAREN),
                Arguments.of(")", TokenType.RIGHT_PAREN),
                Arguments.of("{", TokenType.LEFT_BRACE),
                Arguments.of("}", TokenType.RIGHT_BRACE),
                Arguments.of(",", TokenType.COMMA),
                Arguments.of(".", TokenType.DOT),
                Arguments.of("-", TokenType.MINUS),
                Arguments.of("+", TokenType.PLUS),
                Arguments.of(";", TokenType.SEMICOLON),
                Arguments.of("*", TokenType.STAR),
                Arguments.of("?", TokenType.QUESTION),
                Arguments.of(":", TokenType.COLON),
                Arguments.of("!", TokenType.BANG),
                Arguments.of("!=", TokenType.BANG_EQUAL),
                Arguments.of("=", TokenType.EQUAL),
                Arguments.of("==", TokenType.EQUAL_EQUAL),
                Arguments.of("<", TokenType.LESS),
                Arguments.of("<=", TokenType.LESS_EQUAL),
                Arguments.of(">", TokenType.GREATER),
                Arguments.of(">=", TokenType.GREATER_EQUAL),
                Arguments.of("/", TokenType.SLASH),
                Arguments.of("and", TokenType.AND),
                Arguments.of("class", TokenType.CLASS),
                Arguments.of("else", TokenType.ELSE),
                Arguments.of("false", TokenType.FALSE),
                Arguments.of("for", TokenType.FOR),
                Arguments.of("fun", TokenType.FUN),
                Arguments.of("if", TokenType.IF),
                Arguments.of("nil", TokenType.NIL),
                Arguments.of("or", TokenType.OR),
                Arguments.of("return", TokenType.RETURN),
                Arguments.of("super", TokenType.SUPER),
                Arguments.of("this", TokenType.THIS),
                Arguments.of("true", TokenType.TRUE),
                Arguments.of("var", TokenType.VAR),
                Arguments.of("while", TokenType.WHILE)
        )

        @Suppress("unused")
        @JvmStatic private fun generateSimpleTokens() = SIMPLE_TOKENS
    }
}
