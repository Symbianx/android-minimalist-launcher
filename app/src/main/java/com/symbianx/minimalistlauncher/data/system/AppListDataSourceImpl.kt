package com.symbianx.minimalistlauncher.data.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import com.symbianx.minimalistlauncher.domain.model.App
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementation of [AppListDataSource] using Android PackageManager.
 */
class AppListDataSourceImpl(
    private val context: Context,
) : AppListDataSource {
    override fun getInstalledApps(): Flow<List<App>> =
        callbackFlow {
            val packageManager = context.packageManager

            fun loadApps(): List<App> {
                val mainIntent =
                    Intent(Intent.ACTION_MAIN, null).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }

                val resolveInfoList: List<ResolveInfo> =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.queryIntentActivities(
                            mainIntent,
                            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.queryIntentActivities(mainIntent, 0)
                    }

                Log.d("AppListDataSource", "Found ${resolveInfoList.size} total activities")

                val apps =
                    resolveInfoList
                        .mapNotNull { resolveInfo ->
                            try {
                                val activityInfo = resolveInfo.activityInfo
                                val appInfo = activityInfo.applicationInfo

                                // Skip the launcher itself
                                if (activityInfo.packageName == context.packageName) {
                                    return@mapNotNull null
                                }

                                val launchIntent =
                                    packageManager.getLaunchIntentForPackage(activityInfo.packageName)
                                        ?: return@mapNotNull null

                                // Determine if it's a system app for metadata purposes
                                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                                App(
                                    packageName = activityInfo.packageName,
                                    label = activityInfo.loadLabel(packageManager).toString(),
                                    launchIntent = launchIntent,
                                    isSystemApp = isSystemApp,
                                )
                            } catch (e: Exception) {
                                Log.e("AppListDataSource", "Error loading app: ${e.message}")
                                null
                            }
                        }
                        .distinctBy { it.packageName }
                        .sortedBy { it.label.lowercase() }

                Log.d("AppListDataSource", "Loaded ${apps.size} apps (including system apps)")
                return apps
            }

            // Send initial app list
            trySend(loadApps())

            // Listen for app install/uninstall events
            val receiver =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        context: Context?,
                        intent: Intent?,
                    ) {
                        trySend(loadApps())
                    }
                }

            val filter =
                IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addDataScheme("package")
                }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }

            awaitClose {
                context.unregisterReceiver(receiver)
            }
        }
}
