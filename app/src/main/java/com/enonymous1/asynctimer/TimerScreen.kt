package com.enonymous1.asynctimer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(viewModel: TimerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isRunning = state.run is TimerRun.Running

    val displayText = when (val run = state.run) {
        is TimerRun.Running -> formatDuration(run.remainingSeconds)
        is TimerRun.Cancelled -> formatDuration(run.remainingSeconds)
        is TimerRun.Finished -> "00:00"
        is TimerRun.Idle -> when (val parsed = parseDuration(state.minutesInput, state.secondsInput)) {
            is DurationParse.Valid -> formatDuration(parsed.totalSeconds)
            is DurationParse.Invalid -> "00:00"
        }
    }

    val statusText = when (state.run) {
        is TimerRun.Running -> stringResource(R.string.status_running)
        is TimerRun.Finished -> stringResource(R.string.timer_finished)
        is TimerRun.Cancelled -> stringResource(R.string.timer_cancelled)
        is TimerRun.Idle -> stringResource(R.string.status_idle)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = displayText,
                modifier = Modifier.testTag("timer_display"),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.minutesInput,
                    onValueChange = viewModel::onMinutesChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("minutes_input"),
                    label = { Text(stringResource(R.string.minutes_label)) },
                    singleLine = true,
                    enabled = !isRunning,
                    isError = state.error != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Text(
                    text = ":",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontSize = 34.sp,
                )
                OutlinedTextField(
                    value = state.secondsInput,
                    onValueChange = viewModel::onSecondsChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("seconds_input"),
                    label = { Text(stringResource(R.string.seconds_label)) },
                    singleLine = true,
                    enabled = !isRunning,
                    isError = state.error != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = viewModel::onStart,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("start_button"),
                    enabled = !isRunning,
                ) {
                    Text(stringResource(R.string.start))
                }
                OutlinedButton(
                    onClick = viewModel::onStop,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stop_button"),
                    enabled = isRunning,
                ) {
                    Text(stringResource(R.string.stop))
                }
            }
        }
    }

    val cancelled = state.run as? TimerRun.Cancelled
    if (state.showCancelledDialog && cancelled != null) {
        AlertDialog(
            onDismissRequest = viewModel::onCancelledDialogDismissed,
            title = { Text(stringResource(R.string.timer_cancelled)) },
            text = {
                Text(
                    stringResource(
                        R.string.time_remaining,
                        cancelled.remainingSeconds / 60,
                        cancelled.remainingSeconds % 60,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onCancelledDialogDismissed) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }
}
