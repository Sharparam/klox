package com.sharparam.klox

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

private val LOG by logger("main")

private val errorHandler = object: ErrorHandler {
    private val log by logger("errorHandler")

    private val printer = AstPrinter()

    override var hadError = false

    override var hadRuntimeError = false

    override fun scanError(token: Token, message: String) {
        report(token.line, " in ${token.type}", message)
    }

    override fun scanError(line: Int, message: String) {
        report(line, "", message)
    }

    override fun parseError(token: Token, message: String) = when (token.type) {
        TokenType.EOF -> report(token.line, " at end", message)
        else -> report(token.line, " at '${token.lexeme}'", message)
    }

    override fun runtimeError(e: RuntimeError) {
        log.error("{} [line {}]", e.message ?: "Unknown error.", e.token.line)
        hadRuntimeError = true
    }

    override fun resetError() {
        hadError = false
        hadRuntimeError = false
    }

    private fun report(line: Int, where: String, message: String) {
        log.error("[line {}] Error{}: {}", line, where, message)
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
                run(line)
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

private fun run(code: String) {
    val scanner = Scanner(code, errorHandler)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens, errorHandler)
    val expression = parser.parse()

    if (errorHandler.hadError)
        return

    interpreter.interpret(expression!!)
}
