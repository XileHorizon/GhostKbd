package com.infamousvague.devkeyboard

import java.util.Locale

/**
 * Three small typing helps (Kevin, 2026-11-14), each a switch in Settings › Typing:
 *
 * - **Shift changes a word's capitals** (`shift_case`): with text selected, or the cursor in or against a word (one
 *   just typed too, as SwiftKey has it),
 *   a tap on shift steps that text through Title, ALL CAPS and lower, as SwiftKey does ("one level it capitalizes the
 *   first letter, next the full word and then if you hit it again it goes to all lower on shift off"). [nextCase],
 *   [wordAround].
 * - **Capital I** (`capital_i`): a lone "i", and i'm, i'll, i've, i'd, made capital as the word ends, where autocorrect
 *   has not done it already (it is off, or only suggests). [capitalI].
 * - **Space after punctuation** (`punct_space`, off to start): , . ! ? ; : typed straight after a word bring a space
 *   with them; a space, or another mark ("?!", "...", a closing bracket), typed next takes its place. Never after a
 *   digit (3.14, 10:30). [wantsSpace], [takesSpaceBack].
 *
 * No Android in it (TextPolishTest).
 */
object TextPolish {
    /** A character that belongs to a word for [wordAround]: letters, digits, and an apostrophe inside. */
    private fun wordChar(c: Char) = c.isLetterOrDigit() || c == '\'' || c == '’'

    /**
     * The word the cursor touches: how many of its characters are before the cursor in [before] and after it in
     * [after]. (0, 0) where it touches none.
     */
    fun wordAround(before: CharSequence, after: CharSequence): Pair<Int, Int> {
        var b = 0
        while (b < before.length && wordChar(before[before.length - 1 - b])) b++
        var a = 0
        while (a < after.length && wordChar(after[a])) a++
        // An apostrophe at either edge is a quote, not the word's.
        while (b > 0 && !before[before.length - b].isLetterOrDigit()) b--
        while (a > 0 && !after[a - 1].isLetterOrDigit()) a--
        if (b + a == 0) return 0 to 0
        val word = before.subSequence(before.length - b, before.length).toString() + after.subSequence(0, a)
        return if (word.any { it.isLetter() }) b to a else 0 to 0
    }

    /** The three capitals a word steps through. */
    enum class Case { TITLE, UPPER, LOWER }

    /** [text] with each word's first letter capital and the rest small. */
    fun title(text: String, locale: Locale = Locale.ROOT): String {
        val out = StringBuilder(text.length)
        var start = true
        for (c in text) {
            if (c.isLetter()) {
                out.append(if (start) c.toString().uppercase(locale) else c.toString().lowercase(locale))
                start = false
            } else {
                out.append(c)
                if (!c.isLetterOrDigit() && c != '\'' && c != '’') start = true
            }
        }
        return out.toString()
    }

    /**
     * What a shift tap makes of [text], and which step that is: lower or mixed goes to Title, Title to ALL CAPS, ALL
     * CAPS to lower. A step that would change nothing ("I" as Title is ALL CAPS already) is passed over. Null where
     * the text has no letters.
     */
    fun nextCase(text: String, locale: Locale = Locale.ROOT): Pair<String, Case>? {
        if (text.none { it.isLetter() }) return null
        val lower = text.lowercase(locale)
        val upper = text.uppercase(locale)
        val title = title(text, locale)
        val order = when (text) {
            upper -> listOf(Case.LOWER)
            title -> listOf(Case.UPPER, Case.LOWER)
            else -> listOf(Case.TITLE, Case.UPPER, Case.LOWER)
        }
        for (c in order) {
            val made = when (c) { Case.TITLE -> title; Case.UPPER -> upper; Case.LOWER -> lower }
            if (made != text) return made to c
        }
        return null
    }

    private val I_WORD = Regex("(?:^|[^\\p{L}\\p{N}'’])(i(?:['’](?:m|ll|ve|d))?)$")

    /** The lone i (or i'm, i'll, i've, i'd) that [before] ends with, and what it becomes; null where it ends with none. */
    fun capitalI(before: CharSequence): Pair<String, String>? {
        val m = I_WORD.find(before) ?: return null
        val word = m.groupValues[1]
        return word to "I" + word.substring(1)
    }

    /** The marks that bring a space with them. */
    const val SPACED = ",.!?;:"

    /** The marks that, typed just after an automatic space, take it back: more marks, and closing brackets and quotes. */
    const val TAKES_BACK = ",.!?;:)]}\"'’”»"

    /**
     * Whether [mark], just typed after [before] (which does not hold it), should bring a space: it is one of
     * [SPACED], it follows a letter, and nothing but space or the field's end is after the cursor ([after]).
     */
    fun wantsSpace(before: CharSequence, mark: String, after: CharSequence): Boolean {
        if (mark.length != 1 || mark[0] !in SPACED) return false
        val prev = before.lastOrNull() ?: return false
        if (!prev.isLetter()) return false
        val next = after.firstOrNull()
        return next == null || next.isWhitespace()
    }

    /** Whether [text], typed straight after an automatic space, takes that space back. */
    fun takesSpaceBack(text: String): Boolean = text.length == 1 && text[0] in TAKES_BACK
}
