package com.sharparam.klox

abstract class Expression {
    abstract fun <T> accept(visitor: Visitor<T>): T

    class Binary(val left: Expression, val operator: Token, val right: Expression): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Grouping(val expression: Expression): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Literal(val value: Any?): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    class Unary(val operator: Token, val right: Expression): Expression() {
        override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
    }

    interface Visitor<out T> {
        fun visit(expr: Binary): T
        fun visit(expr: Grouping): T
        fun visit(expr: Literal): T
        fun visit(expr: Unary): T
    }
}
