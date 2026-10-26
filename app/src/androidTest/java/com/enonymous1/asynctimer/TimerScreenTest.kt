package com.enonymous1.asynctimer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Deterministic UI checks only. Countdown timing is covered by the JVM
 * [TimerEngineTest]; this test does not wait for real seconds to pass.
 */
@RunWith(AndroidJUnit4::class)
class TimerScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun startIsEnabledAndStopIsDisabledInitially() {
        composeRule.onNodeWithTag("start_button").assertIsEnabled()
        composeRule.onNodeWithTag("stop_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("timer_display").assertIsDisplayed()
    }

    @Test
    fun emptyInput_showsErrorOnStart() {
        composeRule.onNodeWithTag("start_button").performClick()
        composeRule.onNodeWithText("Enter a time.").assertIsDisplayed()
    }
}
