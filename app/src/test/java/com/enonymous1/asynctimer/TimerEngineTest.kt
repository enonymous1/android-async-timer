package com.enonymous1.asynctimer

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerEngineTest {

    @Test
    fun countsDownOneSecondAtATimeAndFinishes() = runTest {
        val engine = TimerEngine(this)
        engine.start(3)

        assertEquals(TimerRun.Running(3), engine.run.value)
        advanceTimeBy(1000)
        assertEquals(TimerRun.Running(2), engine.run.value)
        advanceTimeBy(1000)
        assertEquals(TimerRun.Running(1), engine.run.value)
        advanceTimeBy(1000)
        assertEquals(TimerRun.Finished, engine.run.value)
    }

    @Test
    fun cancel_keepsRemainingTimeAndStopsTicking() = runTest {
        val engine = TimerEngine(this)
        engine.start(10)
        advanceTimeBy(3000)
        assertEquals(TimerRun.Running(7), engine.run.value)

        engine.cancel()
        assertEquals(TimerRun.Cancelled(7), engine.run.value)
        advanceTimeBy(10_000)
        assertEquals(TimerRun.Cancelled(7), engine.run.value)
    }

    @Test
    fun startWithZeroOrNegative_doesNothing() = runTest {
        val engine = TimerEngine(this)
        engine.start(0)
        engine.start(-5)
        assertEquals(TimerRun.Idle, engine.run.value)
    }

    @Test
    fun restart_replacesThePreviousCountdown() = runTest {
        val engine = TimerEngine(this)
        engine.start(60)
        advanceTimeBy(5000)
        assertEquals(TimerRun.Running(55), engine.run.value)

        engine.start(3)
        assertEquals(TimerRun.Running(3), engine.run.value)
        advanceTimeBy(1000)
        assertEquals(TimerRun.Running(2), engine.run.value)
    }
}
