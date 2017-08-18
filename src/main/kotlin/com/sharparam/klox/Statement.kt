package com.sharparam.klox

abstract class Statement {
    abstract fun <T> accept(visitor: Visitor<T>): T

    class Expression(val expression: com.sharparam.klox.Expression): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Print(val expression: com.sharparam.klox.Expression): Statement() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    interface Visitor<out T> {
        fun visit(stmt: Expression): T
        fun visit(stmt: Print): T
    }
}
