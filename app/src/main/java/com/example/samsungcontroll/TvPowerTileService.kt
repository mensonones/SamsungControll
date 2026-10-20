package com.example.samsungcontroll

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Quick Settings tile that reflects and toggles the saved TV's power state
 * without opening the app.
 *
 * The tile probes the TV over its lightweight HTTP info endpoint whenever the
 * Quick Settings panel opens, so it shows on/off correctly. Tapping it opens a
 * short-lived connection and sends KEY_POWER when the TV is reachable (turning
 * it off), or sends a Wake-On-LAN magic packet when it is unreachable (turning
 * it on). If no TV has been paired yet, it opens the app.
 */
class TvPowerTileService : TileService(), KoinComponent {

    private val tvPreferences: SecureTvPreferences by inject()
    private val quickActions: QuickTvActions by inject()
    private val deviceInfoResolver: SamsungDeviceInfoResolver by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartListening() {
        super.onStartListening()
        refreshState()
    }

    override fun onClick() {
        super.onClick()
        if (tvPreferences.getLastConnectedIp().isBlank()) {
            openApp()
            return
        }
        scope.launch {
            when (quickActions.run(QuickAction.Power)) {
                QuickResult.DONE -> updateTile(on = false)
                QuickResult.WOKE_TV -> updateTile(on = true)
                QuickResult.NEEDS_PAIRING, QuickResult.NO_TV -> withContext(Dispatchers.Main) { openApp() }
                QuickResult.UNREACHABLE -> updateTile(on = false)
            }
        }
    }

    /** Probe the TV's HTTP info endpoint and reflect on/off in the tile. */
    private fun refreshState() {
        val ip = tvPreferences.getLastConnectedIp()
        if (ip.isBlank()) {
            scope.launch { updateTile(on = false) }
            return
        }
        scope.launch {
            val info = deviceInfoResolver.fetchDeviceInfo(ip)
            val isOn = when {
                info == null -> false
                // Prefer the reported power state when the firmware exposes it, so a
                // TV that keeps its HTTP server up in standby (Quick Start) reads as off.
                info.powerState.isNotBlank() -> info.powerState.equals("on", ignoreCase = true)
                else -> true
            }
            updateTile(on = isOn)
        }
    }

    private suspend fun updateTile(on: Boolean) = withContext(Dispatchers.Main) {
        qsTile?.apply {
            state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = getString(R.string.tile_power_label)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = getString(if (on) R.string.tile_power_on else R.string.tile_power_off)
            }
            updateTile()
        }
    }

    // The Intent overload is only used below API 34; newer versions take the PendingIntent branch.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
