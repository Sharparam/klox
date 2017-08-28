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

import com.sharparam.klox.util.logger
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

private val LOG by logger("main")

private val errorHandler = object: ErrorHandler {
    override var hadError = false

    override var hadRuntimeError = false

    override fun scanError(token: Token, message: String) {
        report(token.line, token.column, " in ${token.type}", message)
    }

    override fun scanError(line: Int, column: Int, message: String) {
        report(line, column, "", message)
    }

    override fun parseError(token: Token, message: String) = when (token.type) {
        TokenType.EOF -> report(token.line, token.column, " at end", message)
        else -> report(token.line, token.column, " at '${token.lexeme}'", message)
    }

    override fun runtimeError(e: RuntimeError) {
        System.err.println("${e.message ?: "Unknown error"} [${e.token.line}:${e.token.column}]")
        hadRuntimeError = true
    }

    override fun resetError() {
        hadError = false
        hadRuntimeError = false
    }

    private fun report(line: Int, column: Int, where: String, message: String) {
        System.err.println("[$line:$column] Error$where: $message")
        hadError = true
    }
}

private val interpreter = Interpreter(errorHandler)

fun main(vararg args: String) {
    when (args.size) {
        0 -> runPrompt()
        1 -> runFile(args[0])
        else -> LOG.error("Usage: klox [script]")
    }
}

private fun runPrompt() {
    LOG.debug("Running prompt")
    LOG.info("Welcome to klox, a REPL for Lox. Type code to evaluate it, or ':exit' to exit.")
    loop@ while (true) {
        print("> ")
        val line = readLine()
        when (line) {
            null -> LOG.error("Input was NULL")
            ":exit" -> break@loop
            else -> {
                run(line, true)
                errorHandler.resetError()
            }
        }
    }
}

private fun runFile(path: String) {
    LOG.debug("Running file: {}", path)
    val bytes = Files.readAllBytes(Paths.get(path))
    run(String(bytes, Charset.defaultCharset()))

    if (errorHandler.hadError)
        exitProcess(65)
    else if (errorHandler.hadRuntimeError)
        exitProcess(70)
}

private fun run(code: String, isPrompt: Boolean = false) {
    val scanner = Scanner(code, errorHandler)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens, errorHandler)
    val statements = parser.parse()

    if (errorHandler.hadError)
        return

    if (isPrompt) {
        statements.forEach {
            if (it is Statement.Expression)
                interpreter.interpret(Statement.Expression(Expression.Call(
                        Expression.Literal(interpreter.globals["print"]),
                        Token(TokenType.LEFT_PAREN, "(", null, 1, 6),
                        listOf(it.expression)
                )))
            else
                interpreter.interpret(it)
        }
    } else
        interpreter.interpret(statements)
}
