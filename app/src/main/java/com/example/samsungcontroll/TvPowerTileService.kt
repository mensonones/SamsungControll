package com.example.samsungcontroll

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume

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
    private val certificatePinStore: CertificatePinStore by inject()
    private val wakeOnLanSender: WakeOnLanSender by inject()
    private val deviceInfoResolver: SamsungDeviceInfoResolver by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartListening() {
        super.onStartListening()
        refreshState()
    }

    override fun onClick() {
        super.onClick()
        val ip = tvPreferences.getLastConnectedIp()
        if (ip.isBlank()) {
            openApp()
            return
        }
        val identity = tvPreferences.getLastConnectedIdentity()
        val token = tvPreferences.getToken(ip, identity)
        scope.launch {
            when (attemptPowerToggle(ip, identity, token)) {
                Outcome.POWERED_OFF -> updateTile(on = false)
                Outcome.UNREACHABLE -> {
                    val mac = normalizeMacAddress(tvPreferences.getMacAddress(ip, identity))
                    if (mac != null) {
                        runCatching { wakeOnLanSender.send(mac, ip) }
                            .onFailure { Log.w(TAG, "WOL failed", it) }
                    }
                    updateTile(on = true)
                }
                Outcome.NEEDS_PAIRING -> withContext(Dispatchers.Main) { openApp() }
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
            val isOn = deviceInfoResolver.fetchDeviceInfo(ip) != null
            updateTile(on = isOn)
        }
    }

    private suspend fun attemptPowerToggle(ip: String, identity: String?, token: String?): Outcome {
        var controller: SamsungTvController? = null
        val connected = withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                controller = SamsungTvController(
                    tvIp = ip,
                    token = token,
                    certificatePinStore = certificatePinStore,
                    onTokenReceived = { newToken -> tvPreferences.saveToken(ip, identity, newToken) },
                    onStateChange = { state ->
                        if (cont.isActive) {
                            when (state) {
                                ConnectionState.CONNECTED -> cont.resume(ConnectionState.CONNECTED)
                                ConnectionState.FAILED -> cont.resume(ConnectionState.FAILED)
                                ConnectionState.WAITING_FOR_PERMISSION ->
                                    cont.resume(ConnectionState.WAITING_FOR_PERMISSION)
                                else -> Unit
                            }
                        }
                    }
                ).also { it.connect() }
                cont.invokeOnCancellation { controller?.disconnect() }
            }
        }

        return when (connected) {
            ConnectionState.CONNECTED -> {
                controller?.sendKey("KEY_POWER")
                delay(POWER_KEY_FLUSH_MS)
                controller?.disconnect()
                Outcome.POWERED_OFF
            }
            ConnectionState.WAITING_FOR_PERMISSION -> {
                controller?.disconnect()
                Outcome.NEEDS_PAIRING
            }
            else -> {
                controller?.disconnect()
                Outcome.UNREACHABLE
            }
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

    private enum class Outcome { POWERED_OFF, UNREACHABLE, NEEDS_PAIRING }

    private companion object {
        const val TAG = "TvPowerTile"
        const val CONNECT_TIMEOUT_MS = 4_000L
        const val POWER_KEY_FLUSH_MS = 400L
    }
}
