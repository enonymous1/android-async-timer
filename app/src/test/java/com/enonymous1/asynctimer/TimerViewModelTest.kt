package com.enonymous1.asynctimer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TimerViewModelTest {

    @Before
    fun setUp() {
        // viewModelScope needs a Main dispatcher; Unconfined keeps these
        // synchronous validation tests deterministic.
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emptyInput_showsError() {
        val viewModel = TimerViewModel()
        viewModel.onStart()
        assertEquals("Enter a time.", viewModel.uiState.value.error)
    }

    @Test
    fun zeroInput_showsError() {
        val viewModel = TimerViewModel()
        viewModel.onMinutesChange("0")
        viewModel.onSecondsChange("0")
        viewModel.onStart()
        assertEquals("Enter a time greater than zero.", viewModel.uiState.value.error)
    }

    @Test
    fun validInput_clearsError() {
        val viewModel = TimerViewModel()
        viewModel.onMinutesChange("1")
        viewModel.onSecondsChange("30")
        viewModel.onStart()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun input_isFilteredToTwoDigits() {
        val viewModel = TimerViewModel()
        viewModel.onMinutesChange("12a3-")
        viewModel.onSecondsChange("987")
        assertEquals("12", viewModel.uiState.value.minutesInput)
        assertEquals("98", viewModel.uiState.value.secondsInput)
    }

    @Test
    fun changingInput_clearsPreviousError() {
        val viewModel = TimerViewModel()
        viewModel.onStart()
        assertEquals("Enter a time.", viewModel.uiState.value.error)
        viewModel.onMinutesChange("1")
        assertNull(viewModel.uiState.value.error)
    }
}
