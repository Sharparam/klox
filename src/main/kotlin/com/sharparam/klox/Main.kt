package com.sharparam.klox

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

private val LOG = logger("main")

private var hadError = false

fun main(args: Array<String>) {
    when (args.size) {
        0 -> runPrompt()
        1 -> runFile(args[0])
        else -> LOG.error("Usage: klox [script]")
    }
}

fun runPrompt() {
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
                hadError = false
            }
        }
    }
}

fun runFile(path: String) {
    LOG.debug("Running file: {}", path)
    val bytes = Files.readAllBytes(Paths.get(path))
    run(String(bytes, Charset.defaultCharset()))

    if (hadError)
        exitProcess(65)
}

fun run(code: String) {
    val scanner = Scanner(code)
    scanner.scanTokens().forEach(::println)
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun report(line: Int, where: String, message: String) {
    LOG.error("[line {}] Error{}: {}", line, where, message)
    hadError = true
}
