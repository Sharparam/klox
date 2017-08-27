package com.sharparam.klox

class LoxFunction(private val declaration: Statement.Function): LoxCallable {
    override val arity: Int
        get() = declaration.parameters.count()

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>): Any? {
        val environment = Environment(interpreter.globals)

        (0 until declaration.parameters.count()).forEach {
            environment.define(declaration.parameters.elementAt(it), arguments.elementAt(it))
        }

        try {
            interpreter.execute(declaration.body, environment)
        } catch (e: ReturnException) {
            return e.value
        }

        return null
    }

    override fun toString() = "<fun ${declaration.name.lexeme}>"
}
