package com.sharparam.klox

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnvironmentTests {
    private var environment: Environment = Environment()

    @Before fun setUp() {
        environment = Environment()
    }

    @Test fun shouldDeclareIntVar() = declareTest("foo", 42)

    @Test fun shouldDeclareDoubleVar() = declareTest("foo", 42.9001)

    @Test fun shouldDeclareStringVar() = declareTest("foo", "bar")

    @Test fun shouldDeclareBoolVar() = declareTest("foo", true)

    @Test fun shouldDeclareNilVar() = declareTest("foo", null)

    @Test fun shouldRedeclareIntVar() = redeclareTest("foo", 42, 9001)

    @Test fun shouldRedeclareDoubleVar() = redeclareTest("foo", 42.9001, 9001.42)

    @Test fun shouldRedeclareStringVar() = redeclareTest("foo", "bar", "baz")

    @Test fun shouldRedeclareBoolVar() = redeclareTest("foo", true, false)

    @Test fun shouldNotAssignUndeclaredVar() {
        assertFailsWith<RuntimeError> { environment.assign("foo", 42) }
    }

    @Test fun shouldAssignDeclaredIntVar() = declareAssignTest("foo", 42, 9001)

    @Test fun shouldAssignDeclaredDoubleVar() = declareAssignTest("foo", 42.9001, 9001.42)

    @Test fun shouldAssignDeclaredStringVar() = declareAssignTest("foo", "bar", "baz")

    @Test fun shouldAssignDeclaredBoolVar() = declareAssignTest("foo", true, false)

    @Test fun shouldAssignIntVar() = assignTest("foo", 42, 9001)

    @Test fun shouldAssignDoubleVar() = assignTest("foo", 42.9001, 9001.42)

    @Test fun shouldAssignStringVar() = assignTest("foo", "bar", "baz")

    @Test fun shouldAssignBoolVar() = assignTest("foo", true, false)

    @Test fun shouldDeclareDoubleFromInt() = redeclareTest("foo", 42, 42.9001)

    @Test fun shouldDeclareStringFromInt() = redeclareTest("foo", 42, "bar")

    @Test fun shouldDeclareBoolFromInt() = redeclareTest("foo", 42, true)

    @Test fun shouldDeclareNilFromInt() = redeclareTest("foo", 42, null)

    @Test fun shouldDeclareIntFromDouble() = redeclareTest("foo", 42.9001, 42)

    @Test fun shouldDeclareStringFromDouble() = redeclareTest("foo", 42.9001, "bar")

    @Test fun shouldDeclareBoolFromDouble() = redeclareTest("foo", 42.9001, true)

    @Test fun shouldDeclareNilFromDouble() = redeclareTest("foo", 42.9001, null)

    @Test fun shouldDeclareIntFromString() = redeclareTest("foo", "bar", 42)

    @Test fun shouldDeclareDoubleFromString() = redeclareTest("foo", "bar", 42.9001)

    @Test fun shouldDeclareBoolFromString() = redeclareTest("foo", "bar", true)

    @Test fun shouldDeclareNilFromString() = redeclareTest("foo", "bar", null)

    @Test fun shouldDeclareIntFromBool() = redeclareTest("foo", true, 42)

    @Test fun shouldDeclareDoubleFromBool() = redeclareTest("foo", true, 42.9001)

    @Test fun shouldDeclareStringFromBool() = redeclareTest("foo", true, "bar")

    @Test fun shouldDeclareNilFromBool() = redeclareTest("foo", true, null)

    @Test fun shouldDeclareIntFromNil() = redeclareTest("foo", null, 42)

    @Test fun shouldDeclareDoubleFromNil() = redeclareTest("foo", null, 42.9001)

    @Test fun shouldDeclareStringFromNil() = redeclareTest("foo", null, "bar")

    @Test fun shouldDeclareBoolFromNil() = redeclareTest("foo", null, true)

    @Test fun shouldAssignDoubleFromDeclaredInt() = declareAssignTest("foo", 42, 42.9001)

    @Test fun shouldAssignStringFromDeclaredInt() = declareAssignTest("foo", 42, "bar")

    @Test fun shouldAssignBoolFromDeclaredInt() = declareAssignTest("foo", 42, true)

    @Test fun shouldAssignNilFromDeclaredInt() = declareAssignTest("foo", 42, null)

    @Test fun shouldAssignIntFromDeclaredDouble() = declareAssignTest("foo", 42.9001, 42)

    @Test fun shouldAssignStringFromDeclaredDouble() = declareAssignTest("foo", 42.9001, "bar")

    @Test fun shouldAssignBoolFromDeclaredDouble() = declareAssignTest("foo", 42.9001, true)

    @Test fun shouldAssignNilFromDeclaredDouble() = declareAssignTest("foo", 42.9001, null)

    @Test fun shouldAssignIntFromDeclaredString() = declareAssignTest("foo", "bar", 42)

    @Test fun shouldAssignDoubleFromDeclaredString() = declareAssignTest("foo", "bar", 42.9001)

    @Test fun shouldAssignBoolFromDeclaredString() = declareAssignTest("foo", "bar", true)

    @Test fun shouldAssignNilFromDeclaredString() = declareAssignTest("foo", "bar", null)

    @Test fun shouldAssignIntFromDeclaredBool() = declareAssignTest("foo", true, 42)

    @Test fun shouldAssignDoubleFromDeclaredBool() = declareAssignTest("foo", true, 42.9001)

    @Test fun shouldAssignStringFromDeclaredBool() = declareAssignTest("foo", true, "bar")

    @Test fun shouldAssignNilFromDeclaredBool() = declareAssignTest("foo", true, null)

    @Test fun shouldAssignIntFromDeclaredNil() = declareAssignTest("foo", null, 42)

    @Test fun shouldAssignDoubleFromDeclaredNil() = declareAssignTest("foo", null, 42.9001)

    @Test fun shouldAssignStringFromDeclaredNil() = declareAssignTest("foo", null, "bar")

    @Test fun shouldAssignBoolFromDeclaredNil() = declareAssignTest("foo", null, true)

    @Test fun shouldAssignDoubleFromAssignedInt() = assignTest("foo", 42, 42.9001)

    @Test fun shouldAssignStringFromAssignedInt() = assignTest("foo", 42, "bar")

    @Test fun shouldAssignBoolFromAssignedInt() = assignTest("foo", 42, true)

    @Test fun shouldAssignNilFromAssignedInt() = assignTest("foo", 42, null)

    @Test fun shouldAssignIntFromAssignedDouble() = assignTest("foo", 42.9001, 42)

    @Test fun shouldAssignStringFromAssignedDouble() = assignTest("foo", 42.9001, "bar")

    @Test fun shouldAssignBoolFromAssignedDouble() = assignTest("foo", 42.9001, true)

    @Test fun shouldAssignNilFromAssignedDouble() = assignTest("foo", 42.9001, null)

    @Test fun shouldAssignIntFromAssignedString() = assignTest("foo", "bar", 42)

    @Test fun shouldAssignDoubleFromAssignedString() = assignTest("foo", "bar", 42.9001)

    @Test fun shouldAssignBoolFromAssignedString() = assignTest("foo", "bar", true)

    @Test fun shouldAssignNilFromAssignedString() = assignTest("foo", "bar", null)

    @Test fun shouldAssignIntFromAssignedBool() = assignTest("foo", true, 42)

    @Test fun shouldAssignDoubleFromAssignedBool() = assignTest("foo", true, 42.9001)

    @Test fun shouldAssignStringFromAssignedBool() = assignTest("foo", true, "bar")

    @Test fun shouldAssignNilFromAssignedBool() = assignTest("foo", true, null)

    @Test fun shouldAssignIntFromAssignedNil() = assignTest("foo", null, 42)

    @Test fun shouldAssignDoubleFromAssignedNil() = assignTest("foo", null, 42.9001)

    @Test fun shouldAssignStringFromAssignedNil() = assignTest("foo", null, "bar")

    @Test fun shouldAssignBoolFromAssignedNil() = assignTest("foo", null, true)

    private fun <T> declareTest(key: String, value: T) {
        environment.define(key, value)
        assertEquals(value, environment[key])
    }

    private fun <O, N> assignTest(key: String, oldValue: O, newValue: N) {
        environment.define(key, null)
        environment.assign(key, oldValue)
        environment.assign(key, newValue)
        assertEquals(newValue, environment[key])
    }

    private fun <O, N> redeclareTest(key: String, oldValue: O, newValue: N) {
        environment.define(key, oldValue)
        environment.define(key, newValue)
        assertEquals(newValue, environment[key])
    }

    private fun <O, N> declareAssignTest(key: String, oldValue: O, newValue: N) {
        environment.define(key, oldValue)
        environment.assign(key, newValue)
        assertEquals(newValue, environment[key])
    }

    private fun String.toToken() = Token(TokenType.IDENTIFIER, this, null, 1, 1)

    private operator fun Environment.get(key: String) = this[key.toToken()]

    private fun Environment.define(key: String, value: Any?) = this.define(key.toToken(), value)

    private fun Environment.assign(key: String, value: Any?) = this.assign(key.toToken(), value)
}
