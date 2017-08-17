package com.sharparam.klox

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

private val LOG by logger("main")

fun main(args: Array<String>) {
    when (args.size) {
        0 -> runPrompt()
        1 -> runFile(args[0])
        else -> LOG.error("Usage: klox [script]")
    }
}

val errorHandler = object: ErrorHandler {
    private val log by logger("errorHandler")

    override var hadError: Boolean = false

    override fun scanError(token: Token, message: String) {
        report(token.line, " in ${token.type}", message)
    }

    override fun scanError(line: Int, message: String) {
        report(line, "", message)
    }

    override fun resetError() {
        hadError = false
    }

    private fun report(line: Int, where: String, message: String) {
        log.error("[line {}] Error{}: {}", line, where, message)
        hadError = true
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
}

private fun run(code: String) {
    val scanner = Scanner(code, errorHandler)
    scanner.scanTokens().forEach(::println)
}
