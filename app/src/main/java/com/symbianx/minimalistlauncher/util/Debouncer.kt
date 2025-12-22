package com.symbianx.minimalistlauncher.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Utility to debounce actions in Compose or ViewModel scope.
 * Usage: call debounce with a key and action; only the last call within [timeoutMs] will execute.
 */
class Debouncer(
    private val scope: CoroutineScope,
) {
    private val jobs = mutableMapOf<String, Job>()

    fun debounce(
        key: String,
        timeoutMs: Long = 300,
        action: () -> Unit,
    ) {
        jobs[key]?.cancel()
        jobs[key] =
            scope.launch {
                delay(timeoutMs)
                action()
            }
    }

    fun cancel(key: String) {
        jobs[key]?.cancel()
        jobs.remove(key)
    }

    fun cancelAll() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }
}
