package com.symbianx.minimalistlauncher.ui.settings.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Confirmation dialog for resetting all settings to defaults.
 *
 * @param onConfirm Callback when user confirms the reset
 * @param onDismiss Callback when user cancels or dismisses
 * @param modifier Modifier for the dialog
 */
@Composable
fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reset Settings?",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = "This will restore all settings to their default values. This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier,
    )
}
