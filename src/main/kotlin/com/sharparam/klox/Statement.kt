package com.sharparam.klox

abstract class Statement {
    abstract fun <T> accept(visitor: Visitor<T>): T

    class Expression(val expression: com.sharparam.klox.Expression): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class If(val condition: com.sharparam.klox.Expression, val thenStmt: Statement, val elseStmt: Statement?): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Print(val expression: com.sharparam.klox.Expression): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Variable(val name: Token, val initializer: com.sharparam.klox.Expression): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class While(val condition: com.sharparam.klox.Expression, val body: Statement): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Block(val statements: List<Statement>): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    interface Visitor<out T> {
        fun visit(stmt: Expression): T
        fun visit(stmt: If): T
        fun visit(stmt: Print): T
        fun visit(stmt: Variable): T
        fun visit(stmt: While): T
        fun visit(stmt: Block): T
    }
}
