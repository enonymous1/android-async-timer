package com.enonymous1.asynctimer

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimerUiState(
    val minutesInput: String = "",
    val secondsInput: String = "",
    val run: TimerRun = TimerRun.Idle,
    val error: String? = null,
    val showCancelledDialog: Boolean = false,
)

/**
 * Owns the timer across configuration changes. The old Activity-owned AsyncTask
 * held a strong reference to a destroyed Activity after rotation; this ViewModel
 * survives rotation, and the [TimerEngine] job is a child of [viewModelScope],
 * so it is cancelled when the ViewModel is cleared instead of leaking.
 *
 * Process death is handled partially and honestly: the absolute end time is
 * saved in [SavedStateHandle], so a timer that is still running when the process
 * is recreated resumes with its remaining time. A timer that finished while the
 * process was dead does not fire a notification -- background completion is
 * out of scope (see README) -- so its saved end time is simply cleared.
 */
class TimerViewModel(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    companion object {
        private const val KEY_MINUTES = "minutes_input"
        private const val KEY_SECONDS = "seconds_input"
        private const val KEY_END_ELAPSED = "timer_end_elapsed"
        private const val KEY_TOTAL = "timer_total"
    }

    private val engine = TimerEngine(viewModelScope)

    private val _uiState = MutableStateFlow(
        TimerUiState(
            minutesInput = savedStateHandle[KEY_MINUTES] ?: "",
            secondsInput = savedStateHandle[KEY_SECONDS] ?: "",
        ),
    )
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    init {
        resumeSavedTimer()
        viewModelScope.launch {
            engine.run.collect { run ->
                _uiState.update { state ->
                    var next = state.copy(run = run)
                    if (run is TimerRun.Cancelled) {
                        next = next.copy(showCancelledDialog = true)
                    }
                    if (run is TimerRun.Finished) {
                        clearSavedEnd()
                    }
                    next
                }
            }
        }
    }

    private fun resumeSavedTimer() {
        val endElapsed: Long? = savedStateHandle[KEY_END_ELAPSED]
        val total: Int? = savedStateHandle[KEY_TOTAL]
        if (endElapsed == null || total == null) return
        val remainingMillis = endElapsed - SystemClock.elapsedRealtime()
        if (remainingMillis > 0) {
            // Round up so a timer with half a second left still shows one second.
            val remainingSeconds = ((remainingMillis + 999L) / 1000L).toInt()
            engine.start(remainingSeconds)
        } else {
            clearSavedEnd()
        }
    }

    private fun clearSavedEnd() {
        savedStateHandle.remove<Long>(KEY_END_ELAPSED)
        savedStateHandle.remove<Int>(KEY_TOTAL)
    }

    fun onMinutesChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(2)
        savedStateHandle[KEY_MINUTES] = filtered
        _uiState.update { it.copy(minutesInput = filtered, error = null) }
    }

    fun onSecondsChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(2)
        savedStateHandle[KEY_SECONDS] = filtered
        _uiState.update { it.copy(secondsInput = filtered, error = null) }
    }

    fun onStart() {
        val state = _uiState.value
        if (state.run is TimerRun.Running) return
        when (val parsed = parseDuration(state.minutesInput, state.secondsInput)) {
            is DurationParse.Invalid -> {
                _uiState.update { it.copy(error = parsed.reason) }
            }
            is DurationParse.Valid -> {
                savedStateHandle[KEY_END_ELAPSED] =
                    SystemClock.elapsedRealtime() + parsed.totalSeconds * 1000L
                savedStateHandle[KEY_TOTAL] = parsed.totalSeconds
                _uiState.update { it.copy(error = null) }
                engine.start(parsed.totalSeconds)
            }
        }
    }

    fun onStop() {
        clearSavedEnd()
        engine.cancel()
    }

    fun onCancelledDialogDismissed() {
        savedStateHandle[KEY_MINUTES] = ""
        savedStateHandle[KEY_SECONDS] = ""
        _uiState.update {
            it.copy(
                minutesInput = "",
                secondsInput = "",
                showCancelledDialog = false,
            )
        }
        engine.reset()
    }
}
