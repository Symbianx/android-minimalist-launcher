package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.symbianx.minimalistlauncher.domain.model.FavoriteApp

/**
 * Displays a vertical list of favorite apps on the home screen.
 *
 * @param favorites List of favorite apps to display
 * @param onFavoriteClick Callback when a favorite is tapped
 * @param onFavoriteLongPress Callback when a favorite is long-pressed (for removal)
 * @param modifier Modifier for the favorites list
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesList(
    favorites: List<FavoriteApp>,
    onFavoriteClick: (FavoriteApp) -> Unit,
    onFavoriteLongPress: (FavoriteApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (favorites.isEmpty()) {
        return
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        favorites.sortedBy { it.position }.forEach { favorite ->
            Text(
                text = favorite.label,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onFavoriteClick(favorite) },
                            onLongClick = { onFavoriteLongPress(favorite) },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ).padding(vertical = 20.dp, horizontal = 8.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
