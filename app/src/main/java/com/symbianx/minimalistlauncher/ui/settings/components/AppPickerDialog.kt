package com.symbianx.minimalistlauncher.ui.settings.components

import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.ui.home.HomeViewModel

/**
 * App picker dialog for selecting apps for quick action buttons.
 * Modern Material3 design with smooth corners and clean aesthetics.
 * Shows minimalistic app icons for better visual identification.
 *
 * @param onAppSelected Callback when an app is selected
 * @param onDismiss Callback when dialog is dismissed
 * @param modifier Modifier for the dialog
 * @param homeViewModel ViewModel for accessing app list
 */
@Composable
fun AppPickerDialog(
    onAppSelected: (App) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(),
) {
    var searchQuery by remember { mutableStateOf("") }
    val allApps by homeViewModel.allApps.collectAsState()

    // Filter apps based on search query
    val filteredApps =
        remember(allApps, searchQuery) {
            if (searchQuery.isEmpty()) {
                allApps
            } else {
                allApps.filter {
                    it.label.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
                }
            }
        }

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
    ) {
        Surface(
            modifier =
                modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Title
                Text(
                    text = "Select App",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
                )

                // Search field
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search apps...",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 16.dp),
                )

                // App list
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                ) {
                    items(filteredApps) { app ->
                        AppPickerItem(
                            app = app,
                            onClick = {
                                onAppSelected(app)
                                onDismiss()
                            },
                        )
                    }

                    // Bottom padding for last item
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Individual app item in the picker dialog.
 * Displays a minimalistic monochrome icon alongside the app name.
 */
@Composable
private fun AppPickerItem(
    app: App,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val iconColor = MaterialTheme.colorScheme.onSurface

    // Load app icon (preferring monochrome for minimalist style)
    // Returns Pair<ImageBitmap, Boolean> where Boolean indicates if it's monochrome
    val iconData =
        remember(app.packageName) {
            try {
                val drawable = packageManager.getApplicationIcon(app.packageName)
                
                // Try to extract monochrome icon from adaptive icon (API 33+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
                    drawable is AdaptiveIconDrawable) {
                    val monochromeDrawable = drawable.monochrome
                    if (monochromeDrawable != null) {
                        // Return monochrome icon with true flag
                        Pair(monochromeDrawable.toBitmap(96, 96).asImageBitmap(), true)
                    } else {
                        // Fallback to full icon if no monochrome available
                        Pair(drawable.toBitmap(96, 96).asImageBitmap(), false)
                    }
                } else if (drawable is BitmapDrawable) {
                    Pair(drawable.bitmap.asImageBitmap(), false)
                } else {
                    Pair(drawable.toBitmap(96, 96).asImageBitmap(), false)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // App icon with color tint only for monochrome icons
        if (iconData != null) {
            val (appIcon, isMonochrome) = iconData
            Image(
                bitmap = appIcon,
                contentDescription = "${app.label} icon",
                colorFilter = if (isMonochrome) ColorFilter.tint(iconColor) else null,
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape),
            )
        } else {
            // Fallback: Empty circle placeholder
            Spacer(
                modifier =
                    Modifier
                        .size(36.dp),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // App info
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
