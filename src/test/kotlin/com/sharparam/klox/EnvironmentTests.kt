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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnvironmentTests {
    private var global = Environment()
    private var local = Environment(global)

    @BeforeEach fun setUp() {
        global = Environment()
        local = Environment(global)
    }

    @Test fun shouldNotGetUndeclared() = assertFailsWith<RuntimeError> { global["foo"] }

    @Test fun shouldNotAssignUndeclared() = assertFailsWith<RuntimeError> { global.assign("foo", 42) }

    @Test fun shouldNotExposeChildVarToParent() {
        local.define("foo", "bar")
        assertFailsWith<RuntimeError> { global["foo"] }
    }

    @Test fun shouldNotChangeParentWithChildDeclare() {
        global.define("foo", "bar")
        local.define("foo", "baz")
        assertEquals("bar", global["foo"])
    }

    @ParameterizedTest
    @MethodSource("generateTypes")
    fun shouldGetDeclaredValue(type: KClass<*>) {
        val value = generateValue(type)
        global.define("foo", value)
        assertEquals(value, global["foo"])
    }

    @ParameterizedTest
    @MethodSource("generateTypes")
    fun childShouldGetVarDeclaredInparent(type: KClass<*>) {
        val value = generateValue(type)
        global.define("foo", value)
        assertEquals(value, local["foo"])
    }

    @ParameterizedTest
    @MethodSource("generateTypeCombinations")
    fun shouldRedeclare(oldType: KClass<*>, newType: KClass<*>) {
        val old = generateValue(oldType)
        val new = generateValue(newType, true)
        global.define("foo", old)
        global.define("foo", new)
        assertEquals(new, global["foo"])
    }

    @ParameterizedTest
    @MethodSource("generateTypeCombinations")
    fun shouldAssignToDeclaredVar(oldType: KClass<*>, newType: KClass<*>) {
        val old = generateValue(oldType)
        val new = generateValue(newType, true)
        global.define("foo", old)
        global.assign("foo", new)
        assertEquals(new, global["foo"])
    }

    @ParameterizedTest
    @MethodSource("generateTypeCombinations")
    fun shouldOnlyUseLatestAssignedValue(oldType: KClass<*>, newType: KClass<*>) {
        val old = generateValue(oldType)
        val new = generateValue(newType, true)
        global.define("foo", null)
        global.assign("foo", old)
        global.assign("foo", new)
        assertEquals(new, global["foo"])
    }

    @ParameterizedTest
    @MethodSource("generateTypeCombinations")
    fun shouldUseChildDeclaredValueInChild(parentType: KClass<*>, childType: KClass<*>) {
        val parent = generateValue(parentType)
        val child = generateValue(childType, true)
        global.define("foo", parent)
        local.define("foo", child)
        assertEquals(child, local["foo"])
    }

    @ParameterizedTest
    @MethodSource("generateTypeCombinations")
    fun childShouldChangeVarDeclaredInParent(parentType: KClass<*>, childType: KClass<*>) {
        val parent = generateValue(parentType)
        val child = generateValue(childType)
        global.define("foo", parent)
        local.assign("foo", child)
        assertEquals(child, local["foo"])
    }

    @ParameterizedTest
    @MethodSource("generateTypeCombinations")
    fun shouldChangeParentVarWhenAssignedInChild(parentType: KClass<*>, childType: KClass<*>) {
        val parent = generateValue(parentType)
        val child = generateValue(childType, true)
        global.define("foo", parent)
        local.assign("foo", child)
        assertEquals(child, global["foo"])
    }

    companion object {
        private val TYPES: Array<KClass<*>> = arrayOf(Int::class, Double::class, String::class, Boolean::class, Nothing::class)

        private val TYPE_COMBINATIONS: Array<Arguments> = TYPES.flatMap { first ->
            TYPES.map { second -> Arguments.of(first, second) }
        }.toTypedArray()

        private fun String.toToken() = Token(TokenType.IDENTIFIER, this, null, 1, 1)

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

        @Suppress("unused")
        @JvmStatic private fun generateTypes() = TYPES

        @Suppress("unused")
        @JvmStatic private fun generateTypeCombinations() = TYPE_COMBINATIONS
    }
}
