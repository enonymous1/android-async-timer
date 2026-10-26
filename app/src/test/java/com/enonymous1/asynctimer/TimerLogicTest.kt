package com.enonymous1.asynctimer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerLogicTest {

    @Test
    fun bothFieldsEmpty_isInvalid() {
        assertEquals(
            DurationParse.Invalid("Enter a time."),
            parseDuration("", ""),
        )
    }

    @Test
    fun onlyMinutes_countsAsMinutes() {
        assertEquals(DurationParse.Valid(120), parseDuration("2", ""))
    }

    @Test
    fun onlySeconds_countsAsSeconds() {
        assertEquals(DurationParse.Valid(45), parseDuration("", "45"))
    }

    @Test
    fun bothFields_combine() {
        assertEquals(DurationParse.Valid(125), parseDuration("2", "5"))
    }

    @Test
    fun zeroDuration_isInvalid() {
        assertEquals(
            DurationParse.Invalid("Enter a time greater than zero."),
            parseDuration("0", "0"),
        )
    }

    @Test
    fun negativeInput_isInvalid() {
        // The UI filters to digits, but the parser must not trust it.
        assertTrue(parseDuration("-1", "0") is DurationParse.Invalid)
        assertTrue(parseDuration("1", "-30") is DurationParse.Invalid)
    }

    @Test
    fun nonNumericInput_isInvalid() {
        assertTrue(parseDuration("a", "30") is DurationParse.Invalid)
    }

    @Test
    fun secondsAbove59_normalizeIntoMinutes() {
        // 1 minute 90 seconds is a 150-second timer, displayed as 02:30.
        assertEquals(DurationParse.Valid(150), parseDuration("1", "90"))
        assertEquals("02:30", formatDuration(150))
    }

    @Test
    fun formatDuration_padsAndDoesNotTruncateLargeMinutes() {
        assertEquals("00:00", formatDuration(0))
        assertEquals("01:05", formatDuration(65))
        assertEquals("100:39", formatDuration(6039))
    }

    @Test(expected = IllegalArgumentException::class)
    fun formatDuration_rejectsNegative() {
        formatDuration(-1)
    }
}
