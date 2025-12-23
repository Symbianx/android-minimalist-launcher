package com.symbianx.minimalistlauncher.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UsageTrackingDataSourceImpl(
    context: Context,
) : UsageTrackingDataSource {
    private val prefs: SharedPreferences = context.getSharedPreferences("usage_data", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override fun getUsageData(): UsageData {
        val data = prefs.getString("usage_data", null)
        val today = getToday()
        val usage = if (data != null) {
            try {
                json.decodeFromString<UsageData>(data)
            } catch (e: Exception) {
                UsageData(currentDate = today)
            }
        } else {
            UsageData(currentDate = today)
        }
        return if (usage.currentDate != today) {
            val reset = UsageData(currentDate = today)
            saveUsageData(reset)
            reset
        } else {
            usage
        }
    }

    override fun incrementUnlockCount() {
        val usage = getUsageData()
        val updated = usage.copy(unlockCount = usage.unlockCount + 1, lastUnlockTimestamp = System.currentTimeMillis())
        saveUsageData(updated)
    }

    override fun updateLastUnlockTimestamp(timestamp: Long) {
        val usage = getUsageData()
        saveUsageData(usage.copy(lastUnlockTimestamp = timestamp))
    }

    override fun incrementAppLaunchCount(packageName: String) {
        val usage = getUsageData()
        val appData = usage.appLaunches[packageName] ?: AppUsageData(0, 0)
        val updatedAppData = appData.copy(launchCount = appData.launchCount + 1, lastLaunchTimestamp = System.currentTimeMillis())
        val updated = usage.copy(appLaunches = usage.appLaunches + (packageName to updatedAppData))
        saveUsageData(updated)
    }

    override fun updateAppLastLaunchTimestamp(
        packageName: String,
        timestamp: Long,
    ) {
        val usage = getUsageData()
        val appData = usage.appLaunches[packageName] ?: AppUsageData(0, 0)
        val updatedAppData = appData.copy(lastLaunchTimestamp = timestamp)
        val updated = usage.copy(appLaunches = usage.appLaunches + (packageName to updatedAppData))
        saveUsageData(updated)
    }

    override fun saveUsageData(data: UsageData) {
        prefs.edit().putString("usage_data", json.encodeToString(data)).apply()
    }

    override fun clearUsageData() {
        prefs.edit().remove("usage_data").apply()
    }

    private fun getToday(): String = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
}
