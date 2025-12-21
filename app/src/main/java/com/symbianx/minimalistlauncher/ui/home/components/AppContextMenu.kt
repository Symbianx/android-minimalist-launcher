package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.util.AnimationUtil
import com.symbianx.minimalistlauncher.util.ContextMenuLogger

/**
 * Context menu modal bottom sheet for app actions.
 *
 * @param app The app this context menu is for
 * @param isFavorite Whether the app is currently in favorites
 * @param onDismiss Callback when the menu is dismissed
 * @param onAddToFavorites Callback when "Add to Favorites" is selected
 * @param onRemoveFromFavorites Callback when "Remove from Favorites" is selected
 * @param onOpenAppInfo Callback when "Go to App Info" is selected
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenu(
    app: App,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onAddToFavorites: () -> Unit,
    onRemoveFromFavorites: () -> Unit,
    onOpenAppInfo: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = {
            ContextMenuLogger.logContextMenuDismissed()
            onDismiss()
        },
        sheetState = sheetState,
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(300)) { fullHeight -> fullHeight / 6 },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .semantics { this.contentDescription = "Context menu for ${app.label}" }
            ) {
            // App name header
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            )

            // Add/Remove from Favorites option
            if (isFavorite) {
                ContextMenuItem(
                    text = "Remove from Favorites",
                    onClick = {
                        ContextMenuLogger.logRemoveFromFavorites(app.label)
                        onRemoveFromFavorites()
                        onDismiss()
                    },
                    contentDescription = "Remove ${app.label} from favorites",
                )
            } else {
                ContextMenuItem(
                    text = "Add to Favorites",
                    onClick = {
                        ContextMenuLogger.logAddToFavorites(app.label)
                        onAddToFavorites()
                        onDismiss()
                    },
                    contentDescription = "Add ${app.label} to favorites",
                )
            }

            // Go to App Info option
            ContextMenuItem(
                text = "Go to App Info",
                onClick = {
                    ContextMenuLogger.logOpenAppInfo(app.label)
                    onOpenAppInfo()
                    onDismiss()
                },
                contentDescription = "Open app info for ${app.label}",
            )
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    text: String,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .semantics { this.contentDescription = contentDescription },
        color = MaterialTheme.colorScheme.onSurface,
    )
}
