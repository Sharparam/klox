package com.sharparam.klox

class LoxFunction(
        private val function: Expression.Function,
        private val closure: Environment,
        private val name: String? = null
): LoxCallable {
    override val arity: Int
        get() = function.parameters.count()

    override fun invoke(interpreter: Interpreter, arguments: Iterable<Any?>): Any? {
        val environment = Environment(closure)

        (0 until function.parameters.count()).forEach {
            environment.define(function.parameters.elementAt(it), arguments.elementAt(it))
        }

        try {
            interpreter.execute(function.body, environment)
        } catch (e: ReturnException) {
            return e.value
        }

        return null
    }

    override fun toString() = if (name == null) "<fun>" else "<fun $name>"
}
