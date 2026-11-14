package com.infamousvague.devkeyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** TextPolish.kt: shift's recasing, Capital I and the space after punctuation. */
class TextPolishTest {
    private fun cycle(start: String): List<String> {
        val out = ArrayList<String>()
        var t = start
        repeat(4) { t = TextPolish.nextCase(t)!!.first; out += t }
        return out
    }

    @Test fun `shift steps a word through Title, ALL CAPS and lower`() {
        assertEquals(listOf("Hello", "HELLO", "hello", "Hello"), cycle("hello"))
        assertEquals(listOf("Hello", "HELLO", "hello", "Hello"), cycle("hELlo"))
        assertEquals(TextPolish.Case.UPPER, TextPolish.nextCase("Hello")!!.second)
        assertEquals(TextPolish.Case.LOWER, TextPolish.nextCase("HELLO")!!.second)
        // A one-letter word has no Title of its own: I goes straight to i.
        assertEquals("i" to TextPolish.Case.LOWER, TextPolish.nextCase("I"))
        assertEquals("I" to TextPolish.Case.TITLE, TextPolish.nextCase("i"))
        assertNull(TextPolish.nextCase("123"))
    }

    @Test fun `a selection of words is titled word by word`() {
        assertEquals("Hello There, World", TextPolish.nextCase("hello there, world")!!.first)
        assertEquals("Don't Stop", TextPolish.title("don't stop"))
    }

    @Test fun `the word the cursor touches, from either side or inside`() {
        assertEquals(5 to 0, TextPolish.wordAround("say hello", " there"))
        assertEquals(2 to 3, TextPolish.wordAround("say he", "llo there"))
        assertEquals(0 to 5, TextPolish.wordAround("say ", "hello"))
        assertEquals(0 to 0, TextPolish.wordAround("say ", " hello"))
        assertEquals(5 to 0, TextPolish.wordAround("'don't", ""))
        assertEquals(0 to 0, TextPolish.wordAround("42", ""))
    }

    @Test fun `a lone i and its contractions are capital I`() {
        assertEquals("i" to "I", TextPolish.capitalI("so i"))
        assertEquals("i" to "I", TextPolish.capitalI("i"))
        assertEquals("i'm" to "I'm", TextPolish.capitalI("then i'm"))
        assertEquals("i’ll" to "I’ll", TextPolish.capitalI("(i’ll"))
        assertNull(TextPolish.capitalI("hi"))
        assertNull(TextPolish.capitalI("wifi"))
        assertNull(TextPolish.capitalI("so I"))
    }

    @Test fun `a mark after a word brings a space, but not after a digit or before text`() {
        assertTrue(TextPolish.wantsSpace("hello", ",", ""))
        assertTrue(TextPolish.wantsSpace("hello", "?", " more"))
        assertFalse(TextPolish.wantsSpace("3", ".", ""))
        assertFalse(TextPolish.wantsSpace("10", ":", ""))
        assertFalse(TextPolish.wantsSpace("hello", ".", "com"))
        assertFalse(TextPolish.wantsSpace("hello", "-", ""))
        assertFalse(TextPolish.wantsSpace("hello.", ".", ""))
        assertTrue(TextPolish.takesSpaceBack("!"))
        assertTrue(TextPolish.takesSpaceBack(")"))
        assertFalse(TextPolish.takesSpaceBack("a"))
    }
}
