package com.enonymous1.asynctimer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** The countdown's lifecycle, independent of any Activity or ViewModel. */
sealed interface TimerRun {
    data object Idle : TimerRun
    data class Running(val remainingSeconds: Int) : TimerRun
    data object Finished : TimerRun
    data class Cancelled(val remainingSeconds: Int) : TimerRun
}

/**
 * Drives a countdown on [scope] and reports it as a [StateFlow].
 *
 * The first tick happens one second after start, so the display shows the full
 * entered duration for its first second, matching the original app. Cancellation
 * is explicit: cancelling the underlying job alone would leave the state as
 * [TimerRun.Running], so [cancel] records the remaining time first.
 *
 * This class takes its scope as a parameter so unit tests can drive it with a
 * test scope and virtual time instead of waiting for real seconds.
 */
class TimerEngine(
    private val scope: CoroutineScope,
    private val tickDelayMillis: Long = 1000L,
) {
    private val _run = MutableStateFlow<TimerRun>(TimerRun.Idle)
    val run: StateFlow<TimerRun> = _run.asStateFlow()

    private var job: Job? = null

    fun start(totalSeconds: Int) {
        if (totalSeconds <= 0) return
        job?.cancel()
        job = null
        _run.value = TimerRun.Running(totalSeconds)
        job = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(tickDelayMillis)
                remaining--
                _run.value = if (remaining == 0) {
                    TimerRun.Finished
                } else {
                    TimerRun.Running(remaining)
                }
            }
        }
    }

    fun cancel() {
        val current = _run.value
        if (current is TimerRun.Running) {
            job?.cancel()
            job = null
            _run.value = TimerRun.Cancelled(current.remainingSeconds)
        }
    }

    fun reset() {
        job?.cancel()
        job = null
        _run.value = TimerRun.Idle
    }
}
