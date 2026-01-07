package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UnlockCountDisplay(
    unlockCount: Int,
    lastUnlockTimeAgo: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "$unlockCount unlocks today",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = Color.Gray,
        )
        lastUnlockTimeAgo?.let { time ->
            Text(
                text = "last unlock: $time",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = Color.Gray.copy(alpha = 0.7f),
            )
        }
    }
}
