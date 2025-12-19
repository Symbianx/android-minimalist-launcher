package com.symbianx.minimalistlauncher.data.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementation of [BatteryDataSource] using BroadcastReceiver.
 */
class BatteryDataSourceImpl(
    private val context: Context
) : BatteryDataSource {

    override fun observeBatteryStatus(): Flow<Pair<Int, Boolean>> = callbackFlow {
        fun getBatteryInfo(): Pair<Int, Boolean> {
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            
            val percentage = if (scale > 0) (level * 100) / scale else 0
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
            
            return Pair(percentage, isCharging)
        }

        // Send initial battery status
        trySend(getBatteryInfo())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(getBatteryInfo())
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        
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
