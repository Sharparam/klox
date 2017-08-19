package com.sharparam.klox

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnvironmentTests {
    private var environment: Environment = Environment()

    @BeforeEach fun setUp() {
        environment = Environment()
    }

    @Test fun shouldNotAssignUndeclared() {
        assertFailsWith<RuntimeError> { environment.assign("foo", 42) }
    }

    @TestFactory
    fun environmentTests() = TYPES.flatMap { old ->
        listOf(
                createDeclareTest(old),
                *TYPES.flatMap { new ->
                    listOf(
                            createRedeclareTest(old, new),
                            createDeclareAssignTest(old, new),
                            createAssignTest(old, new)
                    )
                }.toTypedArray()
        )

    }

    private fun createDeclareTest(type: KClass<*>) =
            DynamicTest.dynamicTest("shouldDeclare${type.loxName}") {
                val value = generateValue(type)
                environment.define("foo", value)
                assertEquals(value, environment["foo"])
            }

    private fun createRedeclareTest(oldType: KClass<*>, newType: KClass<*>) =
            DynamicTest.dynamicTest("shouldDeclare${newType.loxName}From${oldType.loxName}") {
                val old = generateValue(oldType)
                val new = generateValue(newType, true)
                environment.define("foo", old)
                environment.define("foo", new)
                assertEquals(new, environment["foo"])
            }

    private fun createDeclareAssignTest(oldType: KClass<*>, newType: KClass<*>) =
            DynamicTest.dynamicTest("shouldAssign${newType.loxName}FromDeclared${oldType.loxName}") {
                val old = generateValue(oldType)
                val new = generateValue(newType, true)
                environment.define("foo", old)
                environment.assign("foo", new)
                assertEquals(new, environment["foo"])
            }

    private fun createAssignTest(oldType: KClass<*>, newType: KClass<*>) =
            DynamicTest.dynamicTest("shouldAssign${newType.loxName}From${oldType.loxName}") {
                val old = generateValue(oldType)
                val new = generateValue(newType, true)
                environment.define("foo", null)
                environment.assign("foo", old)
                environment.assign("foo", new)
                assertEquals(new, environment["foo"])
            }

    companion object {
        private val TYPES: Array<KClass<*>> = arrayOf(Int::class, Double::class, String::class, Boolean::class, Nothing::class)

        private val KClass<*>.loxName get() = when (simpleName) {
            "Void" -> "Nil"
            else -> simpleName
        }

        private fun String.toToken() = Token(TokenType.IDENTIFIER, this, null, 1, 1)

        private operator fun Environment.get(key: String) = this[key.toToken()]

        private fun Environment.define(key: String, value: Any?) = this.define(key.toToken(), value)

        private fun Environment.assign(key: String, value: Any?) = this.assign(key.toToken(), value)

        @Suppress("IMPLICIT_CAST_TO_ANY")
        private fun generateValue(type: KClass<*>, alternate: Boolean = false) = when (type) {
            Int::class -> if (alternate) 9001 else 42
            Double::class -> if (alternate) 9001.42 else 42.9001
            String::class -> if (alternate) "baz" else "bar"
            Boolean::class -> !alternate
            Nothing::class -> null

            else -> throw IllegalArgumentException("Unsupported type.")
        }
    }
}
