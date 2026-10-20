package com.example.samsungcontroll

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** A command that can run without the app UI: widget, launcher shortcut, Quick Settings tile. */
sealed interface QuickAction {
    /** Toggle power: KEY_POWER when the TV answers, Wake-on-LAN when it does not. */
    data object Power : QuickAction

    /** Send one key; fails if the TV is unreachable. */
    data class Key(val key: String) : QuickAction

    /** Wake the TV if needed, then send a key (e.g. KEY_HDMI1). */
    data class WakeAndKey(val key: String) : QuickAction

    /** Wake the TV if needed, then open an app. */
    data class WakeAndApp(val appId: String) : QuickAction

    fun encode(): String = when (this) {
        Power -> "power"
        is Key -> "key:$key"
        is WakeAndKey -> "wake_key:$key"
        is WakeAndApp -> "wake_app:$appId"
    }

    companion object {
        fun decode(value: String?): QuickAction? {
            if (value == null) return null
            if (value == "power") return Power
            val (kind, arg) = value.split(":", limit = 2).takeIf { it.size == 2 } ?: return null
            if (arg.isBlank()) return null
            return when (kind) {
                "key" -> Key(arg)
                "wake_key" -> WakeAndKey(arg)
                "wake_app" -> WakeAndApp(arg)
                else -> null
            }
        }
    }
}

enum class QuickResult { DONE, WOKE_TV, NEEDS_PAIRING, UNREACHABLE, NO_TV }

/**
 * Runs [QuickAction]s with a short-lived connection to the last used TV, independent of the
 * app's own connection. Shared by the widget, launcher shortcuts and the Quick Settings tile.
 */
class QuickTvActions(
    private val tvPreferences: SecureTvPreferences,
    private val certificatePinStore: CertificatePinStore,
    private val wakeOnLanSender: WakeOnLanSender
) {
    suspend fun run(action: QuickAction): QuickResult = withContext(Dispatchers.IO) {
        val ip = tvPreferences.getLastConnectedIp()
        if (ip.isBlank()) return@withContext QuickResult.NO_TV
        val identity = tvPreferences.getLastConnectedIdentity()
        val token = tvPreferences.getToken(ip, identity)

        when (action) {
            QuickAction.Power -> power(ip, identity, token)
            is QuickAction.Key -> sendIfReachable(ip, identity, token) { it.sendKey(action.key) }
            is QuickAction.WakeAndKey -> wakeThen(ip, identity, token) { it.sendKey(action.key) }
            is QuickAction.WakeAndApp -> wakeThen(ip, identity, token) { it.launchApp(action.appId) }
        }
    }

    private suspend fun power(ip: String, identity: String?, token: String?): QuickResult {
        val (controller, state) = connect(ip, identity, token, CONNECT_TIMEOUT_MS)
        return when (state) {
            ConnectionState.CONNECTED -> {
                controller.sendKey("KEY_POWER")
                delay(FLUSH_MS)
                controller.disconnect()
                QuickResult.DONE
            }
            ConnectionState.WAITING_FOR_PERMISSION -> {
                controller.disconnect()
                QuickResult.NEEDS_PAIRING
            }
            else -> {
                controller.disconnect()
                if (sendWakeOnLan(ip, identity)) QuickResult.WOKE_TV else QuickResult.UNREACHABLE
            }
        }
    }

    private suspend fun sendIfReachable(
        ip: String,
        identity: String?,
        token: String?,
        block: (SamsungTvController) -> Unit
    ): QuickResult {
        val (controller, state) = connect(ip, identity, token, CONNECT_TIMEOUT_MS)
        return when (state) {
            ConnectionState.CONNECTED -> {
                block(controller)
                delay(FLUSH_MS)
                controller.disconnect()
                QuickResult.DONE
            }
            ConnectionState.WAITING_FOR_PERMISSION -> {
                controller.disconnect()
                QuickResult.NEEDS_PAIRING
            }
            else -> {
                controller.disconnect()
                QuickResult.UNREACHABLE
            }
        }
    }

    /** Try directly; otherwise wake the TV and keep retrying until its remote service is up. */
    private suspend fun wakeThen(
        ip: String,
        identity: String?,
        token: String?,
        block: (SamsungTvController) -> Unit
    ): QuickResult {
        var woke = false
        val deadline = System.currentTimeMillis() + WAKE_TOTAL_TIMEOUT_MS
        while (true) {
            val (controller, state) = connect(ip, identity, token, CONNECT_TIMEOUT_MS)
            when (state) {
                ConnectionState.CONNECTED -> {
                    // A TV that just woke needs a moment before it accepts launches and keys.
                    if (woke) delay(POST_WAKE_SETTLE_MS)
                    block(controller)
                    delay(FLUSH_MS)
                    controller.disconnect()
                    return QuickResult.DONE
                }
                ConnectionState.WAITING_FOR_PERMISSION -> {
                    controller.disconnect()
                    return QuickResult.NEEDS_PAIRING
                }
                else -> controller.disconnect()
            }
            if (System.currentTimeMillis() >= deadline) return QuickResult.UNREACHABLE
            if (!woke) {
                if (!sendWakeOnLan(ip, identity)) return QuickResult.UNREACHABLE
                woke = true
            }
            delay(WAKE_POLL_MS)
        }
    }

    private fun sendWakeOnLan(ip: String, identity: String?): Boolean {
        val mac = normalizeMacAddress(tvPreferences.getMacAddress(ip, identity)) ?: return false
        return runCatching { wakeOnLanSender.send(mac, ip) }
            .onFailure { Log.w(TAG, "Wake-on-LAN failed", it) }
            .isSuccess
    }

    private suspend fun connect(
        ip: String,
        identity: String?,
        token: String?,
        timeoutMs: Long
    ): Pair<SamsungTvController, ConnectionState?> {
        lateinit var controller: SamsungTvController
        val state = withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                controller = SamsungTvController(
                    tvIp = ip,
                    token = token,
                    certificatePinStore = certificatePinStore,
                    onTokenReceived = { newToken -> tvPreferences.saveToken(ip, identity, newToken) },
                    onStateChange = { newState ->
                        if (cont.isActive && newState in TERMINAL_STATES) cont.resume(newState)
                    }
                )
                cont.invokeOnCancellation { controller.disconnect() }
                controller.connect()
            }
        }
        return controller to state
    }

    private companion object {
        const val TAG = "QuickTvActions"
        const val CONNECT_TIMEOUT_MS = 4_000L
        const val FLUSH_MS = 400L
        const val POST_WAKE_SETTLE_MS = 1_500L
        const val WAKE_POLL_MS = 3_000L
        const val WAKE_TOTAL_TIMEOUT_MS = 45_000L
        val TERMINAL_STATES = setOf(
            ConnectionState.CONNECTED,
            ConnectionState.FAILED,
            ConnectionState.WAITING_FOR_PERMISSION
        )
    }
}
