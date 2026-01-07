package com.symbianx.minimalistlauncher.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.symbianx.minimalistlauncher.domain.model.BatteryThresholdMode
import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import com.symbianx.minimalistlauncher.ui.settings.AppPickerTarget
import com.symbianx.minimalistlauncher.ui.settings.SettingsUiState
import com.symbianx.minimalistlauncher.ui.settings.SettingsViewModel

/**
 * Settings screen composable with scaffold and settings sections.
 *
 * @param onBack Callback when back button is pressed
 * @param modifier Modifier for the settings screen
 * @param viewModel Settings view model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAppPicker by viewModel.showAppPicker.collectAsState()
    val showResetDialog by viewModel.showResetDialog.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is SettingsUiState.Loaded -> {
                SettingsContent(
                    settings = state.settings,
                    onAutoLaunchChange = viewModel::updateAutoLaunch,
                    onLeftQuickActionClick = { viewModel.showAppPickerFor(AppPickerTarget.LEFT) },
                    onRightQuickActionClick = { viewModel.showAppPickerFor(AppPickerTarget.RIGHT) },
                    onBatteryModeChange = viewModel::updateBatteryMode,
                    onResetClick = viewModel::showResetDialog,
                    modifier = Modifier.padding(paddingValues),
                )
            }

            is SettingsUiState.Error -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    // App picker dialog
    showAppPicker?.let { target ->
        AppPickerDialog(
            onAppSelect = { app ->
                when (target) {
                    AppPickerTarget.LEFT -> viewModel.updateLeftQuickAction(app)
                    AppPickerTarget.RIGHT -> viewModel.updateRightQuickAction(app)
                }
            },
            onDismiss = viewModel::dismissAppPicker,
        )
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        ResetConfirmationDialog(
            onConfirm = viewModel::resetToDefaults,
            onDismiss = viewModel::dismissResetDialog,
        )
    }
}

/**
 * Settings content with all preference sections.
 */
@Composable
private fun SettingsContent(
    settings: LauncherSettings,
    onAutoLaunchChange: (Boolean) -> Unit,
    onLeftQuickActionClick: () -> Unit,
    onRightQuickActionClick: () -> Unit,
    onBatteryModeChange: (BatteryThresholdMode) -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
    ) {
        // Auto-launch section
        item {
            SwitchPreference(
                title = "Auto-launch apps",
                checked = settings.autoLaunchEnabled,
                onCheckedChange = onAutoLaunchChange,
                description = "Automatically open app when search returns single result",
            )
        }

        item { HorizontalDivider() }

        // Quick actions section
        item {
            PreferenceItem(
                title = "Left quick action",
                description = settings.leftQuickAction.label,
                onClick = onLeftQuickActionClick,
            )
        }

        item {
            PreferenceItem(
                title = "Right quick action",
                description = settings.rightQuickAction.label,
                onClick = onRightQuickActionClick,
            )
        }

        item { HorizontalDivider() }

        // Battery indicator section
        item {
            BatteryThresholdPreference(
                title = "Battery indicator",
                selectedMode = settings.batteryIndicatorMode,
                onModeSelect = onBatteryModeChange,
            )
        }

        item { HorizontalDivider() }

        // Reset section
        item {
            PreferenceItem(
                title = "Reset to defaults",
                description = "Restore all settings to default values",
                onClick = onResetClick,
            )
        }
    }
}

/**
 * Switch preference composable for boolean settings.
 */
@Composable
private fun SwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onCheckedChange(!checked) },
                ).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

/**
 * Preference item composable for clickable settings.
 */
@Composable
private fun PreferenceItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Battery threshold preference with radio group style.
 */
@Composable
private fun BatteryThresholdPreference(
    title: String,
    selectedMode: BatteryThresholdMode,
    onModeSelect: (BatteryThresholdMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))

        BatteryThresholdMode.values().forEach { mode ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onModeSelect(mode) },
                        ).padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.RadioButton(
                    selected = mode == selectedMode,
                    onClick = { onModeSelect(mode) },
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text =
                        when (mode) {
                            BatteryThresholdMode.ALWAYS -> "Always show"
                            BatteryThresholdMode.BELOW_50 -> "Show below 50%"
                            BatteryThresholdMode.BELOW_20 -> "Show below 20%"
                            BatteryThresholdMode.NEVER -> "Never show"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
