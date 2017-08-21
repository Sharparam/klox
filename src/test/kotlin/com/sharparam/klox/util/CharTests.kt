package com.sharparam.klox.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CharTests {
    @ParameterizedTest
    @MethodSource("createAlphaChars")
    fun shouldBeAlpha(char: Char) {
        assertTrue(char.isAlpha)
    }

    @ParameterizedTest
    @MethodSource("createControlChars")
    fun shouldNotBeAlpha(char: Char) {
        assertFalse(char.isAlpha)
    }

    @Test fun digitsShouldNotBeAlpha() = ('0'..'9').forEach {
        assertFalse(it.isAlpha, "$it should not be considered alpha")
    }

    @ParameterizedTest
    @MethodSource("createAlphaNumericChars")
    fun shouldBeAlphaNumeric(char: Char) {
        assertTrue(char.isAlphaNumeric)
    }

    @ParameterizedTest
    @MethodSource("createControlChars")
    fun shouldNotBeAlphaNumeric(char: Char) {
        assertFalse(char.isAlphaNumeric)
    }

    companion object {
        @JvmStatic private fun createAlphaChars(): Array<Char> {
            val chars = ArrayList<Char>()

            chars.addAll('a'..'z')
            chars.addAll('A'..'Z')
            chars.add('_')

            return chars.toTypedArray()
        }

        @Suppress("unused")
        @JvmStatic private fun createAlphaNumericChars(): Array<Char> {
            val chars = ArrayList<Char>()

            chars.addAll(createAlphaChars())
            chars.addAll('0'..'9')

            return chars.toTypedArray()
        }

        @Suppress("unused")
        @JvmStatic private fun createControlChars(): Array<Char> = arrayOf(
                '?', ':', ';', '<', '>', '=', '-', '/', '+', '*', '"', '\'', '(', ')', '[',']', '{', '}',
                ',', '.', '|', '\\', '!', '@', '#', '$', '%', '^', '&'
        )
    }
}
