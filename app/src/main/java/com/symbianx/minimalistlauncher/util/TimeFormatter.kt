package com.symbianx.minimalistlauncher.util

object TimeFormatter {
    fun formatRelativeTime(timestampMillis: Long): String? {
        if (timestampMillis == 0L) return null
        val now = System.currentTimeMillis()
        val diffMillis = now - timestampMillis
        val diffMinutes = diffMillis / (60 * 1000)
        val diffHours = diffMillis / (60 * 60 * 1000)
        return when {
            diffMinutes < 1 -> "just now"
            diffMinutes < 60 -> "${diffMinutes}m ago"
            diffHours < 24 -> "${diffHours}h ago"
            else -> null // Different day
        }
    }
}
