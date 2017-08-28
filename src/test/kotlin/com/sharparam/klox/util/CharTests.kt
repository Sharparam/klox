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
