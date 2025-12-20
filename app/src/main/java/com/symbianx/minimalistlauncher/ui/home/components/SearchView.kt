package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.SearchState
import com.symbianx.minimalistlauncher.domain.usecase.AutoLaunchDecider
import com.symbianx.minimalistlauncher.util.NavigationLogger
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.delay

/**
 * Search view with text input and results display.
 *
 * @param searchState Current search state
 * @param onQueryChange Callback when search query changes
 * @param onAppClick Callback when an app is selected
 * @param onAppLongPress Callback when an app is long-pressed (for adding to favorites)
 * @param onSwipeBack Callback when user swipes back from search to home
 * @param modifier Modifier for the search view
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchView(
    modifier: Modifier = Modifier,
    searchState: SearchState,
    onQueryChange: (String) -> Unit,
    onAppClick: (App) -> Unit,
    onAppLongPress: (App) -> Unit = {},
    onSwipeBack: () -> Unit = {},
    autoLaunchEnabled: Boolean = true,
    autoLaunchDelayMs: Long = 300,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    AnimatedVisibility(
        visible = searchState.isActive,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier,
    ) {
        val haptic = LocalHapticFeedback.current
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (totalDrag > 200f) {
                                    NavigationLogger.logSwipeBack()
                                    focusManager.clearFocus()
                                    onSwipeBack()
                                }
                                totalDrag = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            }
                        )
                    },
        ) {
            TextField(
                value = searchState.query,
                onValueChange = onQueryChange,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .semantics {
                            contentDescription = "Search apps"
                        },
                placeholder = { Text("Search apps...") },
                singleLine = true,
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    ),
            )

            if (searchState.results.isEmpty()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No apps found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                ) {
                    items(searchState.results) { app ->
                        Text(
                            text = app.label,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onAppClick(app) },
                                        onLongClick = { onAppLongPress(app) },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    )
                                    .padding(vertical = 20.dp, horizontal = 8.dp),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
            }
        }

        LaunchedEffect(searchState.isActive) {
            if (searchState.isActive) {
                focusRequester.requestFocus()
            }
        }

        // Auto-Launch: when exactly one result and user pauses typing, launch it
        LaunchedEffect(searchState.query, searchState.results.size, searchState.isActive, autoLaunchEnabled, autoLaunchDelayMs) {
            val hasSingle = AutoLaunchDecider.isEligible(autoLaunchEnabled, searchState)
            if (hasSingle) {
                // Debounce: wait briefly to ensure user paused input
                delay(autoLaunchDelayMs)
                // Re-check single result after debounce
                if (AutoLaunchDecider.isEligible(autoLaunchEnabled, searchState)) {
                    // Provide brief haptic feedback before launching
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAppClick(searchState.results.first())
                }
            }
        }
    }
}
