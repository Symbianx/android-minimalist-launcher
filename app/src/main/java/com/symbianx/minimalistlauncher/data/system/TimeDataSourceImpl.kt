package com.symbianx.minimalistlauncher.data.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementation of [TimeDataSource] using BroadcastReceiver.
 *
 * Listens for system broadcasts:
 * - [Intent.ACTION_TIME_TICK]: sent every minute by the system
 * - [Intent.ACTION_TIME_CHANGED]: sent when the user manually changes the time
 * - [Intent.ACTION_TIMEZONE_CHANGED]: sent when the timezone changes
 */
class TimeDataSourceImpl(
    private val context: Context,
) : TimeDataSource {
    override fun observeTimeTicks(): Flow<Unit> =
        callbackFlow {
            // Emit initial tick so the clock shows immediately
            trySend(Unit)

            val receiver =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        context: Context?,
                        intent: Intent?,
                    ) {
                        trySend(Unit)
                    }
                }

            val filter =
                IntentFilter().apply {
                    addAction(Intent.ACTION_TIME_TICK)
                    addAction(Intent.ACTION_TIME_CHANGED)
                    addAction(Intent.ACTION_TIMEZONE_CHANGED)
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
