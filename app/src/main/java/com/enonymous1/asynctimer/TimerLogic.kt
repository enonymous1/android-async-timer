package com.enonymous1.asynctimer

import java.util.Locale

/**
 * Pure timer math, kept free of Android types so it can be unit-tested on the JVM.
 *
 * The original app accepted up to two digits per field, so a seconds value above 59
 * was already possible; it simply counted down from minutes * 60 + seconds. This
 * parser keeps that meaning instead of rejecting it: "1:90" is a 150-second timer,
 * and the countdown display normalizes it to 02:30.
 */
sealed interface DurationParse {
    data class Valid(val totalSeconds: Int) : DurationParse
    data class Invalid(val reason: String) : DurationParse
}

fun parseDuration(minutesText: String, secondsText: String): DurationParse {
    val minutesRaw = minutesText.trim()
    val secondsRaw = secondsText.trim()
    if (minutesRaw.isEmpty() && secondsRaw.isEmpty()) {
        return DurationParse.Invalid("Enter a time.")
    }
    val minutes = parseField(minutesRaw) ?: return DurationParse.Invalid("Enter whole numbers only.")
    val seconds = parseField(secondsRaw) ?: return DurationParse.Invalid("Enter whole numbers only.")
    val total = minutes.toLong() * 60L + seconds.toLong()
    if (total == 0L) {
        return DurationParse.Invalid("Enter a time greater than zero.")
    }
    if (total > Int.MAX_VALUE) {
        return DurationParse.Invalid("That timer is too long.")
    }
    return DurationParse.Valid(total.toInt())
}

/** An empty field counts as zero; anything that is not plain digits is invalid. */
private fun parseField(raw: String): Int? {
    if (raw.isEmpty()) return 0
    if (!raw.all { it.isDigit() }) return null
    return raw.toIntOrNull()
}

/** Always at least two digits per side; minutes above 99 are not truncated. */
fun formatDuration(totalSeconds: Int): String {
    require(totalSeconds >= 0) { "totalSeconds must not be negative" }
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(Locale.US, minutes, seconds)
}
