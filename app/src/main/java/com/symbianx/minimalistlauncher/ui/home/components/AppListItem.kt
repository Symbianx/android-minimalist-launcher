package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.util.ContextMenuLogger

/**
 * Individual app list item with click and long-press support.
 *
 * @param app The app to display
 * @param onClick Callback when the app is tapped
 * @param onLongClick Callback when the app is long-pressed
 * @param modifier Modifier for the list item
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: App,
    onClick: (App) -> Unit,
    onLongClick: (App) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Text(
        text = app.label,
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick(app) },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        ContextMenuLogger.logContextMenuOpened(app.label)
                        onLongClick(app)
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                )
                .padding(vertical = 20.dp, horizontal = 8.dp),
        style = MaterialTheme.typography.headlineSmall,
    )
}
