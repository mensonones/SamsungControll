package com.example.samsungcontroll

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemoteViewModel(
    application: Application,
    private val tvPreferences: SecureTvPreferences = SecureTvPreferences(application),
    private val discoveryService: DiscoveryService = TvDiscovery(application),
    private val samsungDeviceInfoResolver: SamsungDeviceInfoResolver = SamsungDeviceInfoResolver(),
    private val wakeOnLanSender: WakeOnLanSender = WakeOnLanSender(application),
    private val appSettings: AppSettings = AppSettings(application)
) : AndroidViewModel(application) {

    var ipAddress by mutableStateOf("")
        private set

    var macAddress by mutableStateOf("")
        private set

    var tvNickname by mutableStateOf("")
        private set

    var connectionState by mutableStateOf(ConnectionState.DISCONNECTED)
        private set
        
    var discoveredTvs by mutableStateOf<List<DiscoveredTv>>(emptyList())
        private set
        
    var isSearching by mutableStateOf(false)
        private set
        
    var showDiscovery by mutableStateOf(false)
        private set

    var isMuted by mutableStateOf(false)
        private set

    var savedTvs by mutableStateOf<List<SavedTv>>(emptyList())
        private set

    var hapticsEnabled by mutableStateOf(appSettings.hapticsEnabled)
        private set

    var backgroundControlEnabled by mutableStateOf(appSettings.backgroundControlEnabled)
        private set

    var volumeKeysEnabled by mutableStateOf(appSettings.volumeKeysEnabled)
        private set

    var idleTimeoutMinutes by mutableStateOf(appSettings.idleTimeoutMinutes)
        private set

    var favoriteAppIds by mutableStateOf(appSettings.favoriteAppIds)
        private set

    var installedApps by mutableStateOf<List<TvApp>>(emptyList())
        private set

    /** True while the TV has a text field focused (on-screen keyboard open on the TV). */
    var tvTextInputActive by mutableStateOf(false)
        private set

    var connectionError by mutableStateOf<TvConnectionException?>(null)
        private set

    var tvDeviceInfo by mutableStateOf<TvDeviceInfo?>(null)
        private set
    
    private var controller: TvController? = null
    private var connectionAttempt = 0

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var disconnectReceiver: BroadcastReceiver? = null

    fun initialize() {
        val lastIp = tvPreferences.getLastConnectedIp()
        val lastIdentity = tvPreferences.getLastConnectedIdentity()
        // Migrate: the TV paired before the multi-TV list existed becomes its first entry.
        if (lastIp.isNotBlank() && tvPreferences.getSavedTvs().isEmpty()) {
            tvPreferences.upsertSavedTv(lastIp, lastIdentity)
        }
        refreshSavedTvs()
        macAddress = tvPreferences.getMacAddress(lastIp, lastIdentity)
        tvNickname = tvPreferences.getCustomNickname(lastIp, lastIdentity) ?: ""
        if (lastIp.isNotBlank() && connectionState == ConnectionState.DISCONNECTED) {
            ipAddress = lastIp
            // Direct attempt first; Wake-on-LAN only if the TV doesn't answer.
            connectToTv(lastIp, lastIdentity, wakeOnFailure = true, retriesAfterWake = 0, rediscover = true)
        }
        registerNetworkMonitoring()
        registerDisconnectReceiver()
    }

    private fun registerDisconnectReceiver() {
        if (disconnectReceiver != null) return
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    TvConnectionService.ACTION_DISCONNECT -> disconnect()
                    TvConnectionService.ACTION_VOLUME -> {
                        // Hardware volume pressed while the app is in the background / screen off.
                        val direction = intent.getIntExtra(TvConnectionService.EXTRA_VOLUME_DIRECTION, 0)
                        if (volumeKeysEnabled && connectionState == ConnectionState.CONNECTED && direction != 0) {
                            sendKey(if (direction > 0) "KEY_VOLUP" else "KEY_VOLDOWN")
                        }
                    }
                }
            }
        }
        ContextCompat.registerReceiver(
            getApplication<Application>(),
            receiver,
            IntentFilter().apply {
                addAction(TvConnectionService.ACTION_DISCONNECT)
                addAction(TvConnectionService.ACTION_VOLUME)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        disconnectReceiver = receiver
    }

    private fun registerNetworkMonitoring() {
        if (networkCallback != null) return
        val manager = connectivityManager ?: return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // A network came up (e.g. Wi-Fi reconnected or switched). Try to
                // restore the connection if it isn't already active.
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    reconnectIfNeeded()
                }
            }
        }
        runCatching { manager.registerDefaultNetworkCallback(callback) }
            .onSuccess { networkCallback = callback }
            .onFailure { Log.w("RemoteViewModel", "Unable to register network callback", it) }
    }

    private fun reconnectIfNeeded() {
        if (connectionState == ConnectionState.CONNECTED ||
            connectionState == ConnectionState.CONNECTING
        ) {
            return
        }
        if (controller?.isConnected() == true) return
        val lastIp = tvPreferences.getLastConnectedIp()
        val lastIdentity = tvPreferences.getLastConnectedIdentity()
        if (lastIp.isNotBlank()) {
            ipAddress = lastIp
            connectToTv(lastIp, lastIdentity, wakeOnFailure = false, retriesAfterWake = 0, rediscover = true)
        }
    }

    fun onResume() {
        if (controller?.isConnected() == true && connectionState == ConnectionState.CONNECTED) {
            return
        }
        if (connectionState == ConnectionState.CONNECTING) return
        val lastIp = tvPreferences.getLastConnectedIp()
        val lastIdentity = tvPreferences.getLastConnectedIdentity()
        if (lastIp.isNotBlank()) {
            ipAddress = lastIp
            // Try a direct reconnect first (instant when the TV is on) and only fall
            // back to the slower Wake-On-LAN flow if the direct attempt fails.
            connectToTv(lastIp, lastIdentity, wakeOnFailure = true, retriesAfterWake = 0, rediscover = true)
        }
    }

    private fun refreshSavedTvs() {
        savedTvs = tvPreferences.getSavedTvs()
    }

    /** Label shown in the quick-switch menu: nickname, else the TV's own name, else its IP. */
    fun displayName(tv: SavedTv): String =
        tvPreferences.getCustomNickname(tv.ip, tv.identity) ?: tv.name ?: tv.ip

    fun isCurrentTv(tv: SavedTv): Boolean =
        tv.ip == ipAddress.trim() ||
            (tv.identity != null && tv.identity == tvPreferences.getLastConnectedIdentity())

    fun switchToTv(tv: SavedTv) {
        connectToTv(tv.ip, tv.identity, wakeOnFailure = true, retriesAfterWake = 0, rediscover = true)
    }

    /** Removes a TV from the list and erases its pairing (token and pinned certificate). */
    fun forgetTv(tv: SavedTv) {
        val wasCurrent = isCurrentTv(tv)
        tvPreferences.forgetPairing(tv.ip, tv.identity)
        tvPreferences.removeSavedTv(tv)
        refreshSavedTvs()
        if (wasCurrent) {
            disconnect()
            installedApps = emptyList()
            tvDeviceInfo = null
            ipAddress = ""
            macAddress = ""
            tvNickname = ""
            tvPreferences.saveLastConnectedIp("")
            tvPreferences.saveLastConnectedIdentity(null)
        }
    }

    fun saveTvNickname(nickname: String) {
        val cleanNickname = nickname.trim()
        tvNickname = cleanNickname
        val identity = tvPreferences.getLastConnectedIdentity()
        tvPreferences.saveCustomNickname(cleanNickname, ipAddress, identity)
    }

    fun toggleMute() {
        if (connectionState == ConnectionState.CONNECTED) {
            isMuted = !isMuted
            sendKey("KEY_MUTE")
        }
    }

    fun updateIpAddress(newIp: String) {
        ipAddress = newIp
    }

    fun toggleDiscovery() {
        showDiscovery = !showDiscovery
    }

    fun searchTvs() {
        viewModelScope.launch {
            // Hide any previous results while scanning: during the search the only
            // feedback is the orbit on the magnifier. Results are revealed on finish.
            showDiscovery = false
            isSearching = true
            val start = System.currentTimeMillis()
            discoveredTvs = discoveryService.discoverTvs()
            // Enforce a minimum visible search window. Discovery can return almost
            // instantly (cached/empty); without this the searching state would
            // collapse in a single frame and never render (no orbit, results flash in).
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < MIN_SEARCH_DURATION_MS) delay(MIN_SEARCH_DURATION_MS - elapsed)
            isSearching = false
            showDiscovery = true
        }
    }

    fun connectToTv(tv: DiscoveredTv) {
        tv.macAddress?.let { discoveredMac ->
            tvPreferences.saveMacAddress(discoveredMac, tv.ip, tv.identity)
            macAddress = discoveredMac
        }
        connectToTv(tv.ip, tv.identity)
    }

    fun connectToTv(ip: String, identity: String? = null) {
        connectToTv(ip, identity, wakeOnFailure = false, retriesAfterWake = 0)
    }

    private fun connectToTv(
        ip: String,
        identity: String? = null,
        wakeOnFailure: Boolean,
        retriesAfterWake: Int,
        rediscover: Boolean = false
    ) {
        val cleanIp = ip.trim()
        if (cleanIp.isBlank()) return

        if (!isLocalNetworkHost(cleanIp)) {
            connectionState = ConnectionState.FAILED
            return
        }

        val currentAttempt = ++connectionAttempt
        val lastIp = tvPreferences.getLastConnectedIp()
        val savedIdentity = identity ?: tvPreferences.getLastConnectedIdentity().takeIf { cleanIp == lastIp }
        val savedToken = tvPreferences.getToken(cleanIp, savedIdentity)
        tvPreferences.saveLastConnectedIp(cleanIp)
        tvPreferences.saveLastConnectedIdentity(savedIdentity)
        macAddress = tvPreferences.getMacAddress(cleanIp, savedIdentity)
        tvNickname = tvPreferences.getCustomNickname(cleanIp, savedIdentity) ?: ""
        controller?.disconnect()
        connectionError = null
        installedApps = emptyList()
        tvTextInputActive = false
        ipAddress = cleanIp
        
        controller = SamsungTvController(
            tvIp = cleanIp,
            token = savedToken,
            certificatePinStore = tvPreferences,
            onTokenReceived = { newToken ->
                tvPreferences.saveToken(cleanIp, savedIdentity, newToken)
            },
            onError = { error ->
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    if (currentAttempt == connectionAttempt) connectionError = error
                }
            },
            onInstalledApps = { apps ->
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    if (currentAttempt == connectionAttempt) installedApps = apps
                }
            },
            onTextInputChange = { active ->
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    if (currentAttempt == connectionAttempt) tvTextInputActive = active
                }
            },
            onStateChange = { newState ->
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    if (currentAttempt == connectionAttempt) {
                        val certMismatch = connectionError is TvConnectionException.CertificateMismatch
                        if (newState == ConnectionState.FAILED && certMismatch) {
                            // Waking the TV can't fix a changed certificate; surface it.
                            connectionState = newState
                            stopConnectionService()
                        } else {
                            if (newState == ConnectionState.FAILED && rediscover && savedIdentity != null) {
                                // The TV may have a new DHCP address: look it up by its SSDP
                                // identity while the normal wake/retry flow keeps going.
                                rediscoverByIdentity(currentAttempt, savedIdentity, cleanIp)
                            }
                            handleStateChange(newState, wakeOnFailure, retriesAfterWake, currentAttempt, cleanIp, savedIdentity)
                        }
                    }
                }
            }
        ).apply { connect() }
    }

    private fun handleStateChange(
        newState: ConnectionState,
        wakeOnFailure: Boolean,
        retriesAfterWake: Int,
        currentAttempt: Int,
        cleanIp: String,
        savedIdentity: String?
    ) {
        if (newState == ConnectionState.FAILED && wakeOnFailure && macAddress.isNotBlank()) {
            connectionState = ConnectionState.CONNECTING
            wakeAndReconnect(currentAttempt, cleanIp, savedIdentity)
        } else if (newState == ConnectionState.FAILED && retriesAfterWake > 0) {
            connectionState = ConnectionState.CONNECTING
            retryConnectAfterWake(currentAttempt, cleanIp, savedIdentity, retriesAfterWake - 1)
        } else {
            connectionState = newState
            if (newState == ConnectionState.CONNECTED) {
                tvPreferences.upsertSavedTv(cleanIp, savedIdentity)
                refreshSavedTvs()
                showDiscovery = false
                saveResolvedMacAddress(cleanIp, savedIdentity)
                startConnectionService()
                controller?.requestInstalledApps()
                pendingScene?.let { scene ->
                    pendingScene = null
                    // A TV that just woke needs a moment before it accepts keys and launches.
                    viewModelScope.launch {
                        delay(SCENE_SETTLE_MS)
                        perform(scene)
                    }
                }
                startIdleWatch()
            } else {
                stopConnectionService()
            }
        }
    }

    /**
     * Looks for the TV by its SSDP identity (UUID). If it answers from another address
     * (new DHCP lease), moves the saved IP and pinned certificate over and reconnects there.
     */
    private fun rediscoverByIdentity(attempt: Int, identity: String, oldIp: String) {
        viewModelScope.launch {
            val found = discoveryService.discoverTvs().firstOrNull { it.identity == identity }
            if (found == null || found.ip == oldIp || attempt != connectionAttempt) return@launch

            Log.d("RemoteViewModel", "TV moved from $oldIp to ${found.ip}")
            // Keep the TOFU pin: the new address inherits the certificate pinned for the old one.
            tvPreferences.getCertificateFingerprint(oldIp)?.let {
                tvPreferences.saveCertificateFingerprint(found.ip, it)
            }
            connectToTv(found.ip, identity, wakeOnFailure = false, retriesAfterWake = WAKE_RECONNECT_RETRIES)
        }
    }

    fun reconnect() {
        if (ipAddress.isNotBlank()) {
            connectToTv(
                ipAddress,
                tvPreferences.getLastConnectedIdentity(),
                wakeOnFailure = true,
                retriesAfterWake = 0,
                rediscover = true
            )
        }
    }

    /**
     * Clears the pinned certificate and token of the current TV and connects again,
     * e.g. after a firmware update regenerated the TV's certificate.
     */
    fun forgetPairingAndReconnect() {
        val ip = ipAddress.trim()
        if (ip.isBlank()) return
        val identity = tvPreferences.getLastConnectedIdentity()
        tvPreferences.forgetPairing(ip, identity)
        connectToTv(ip, identity, wakeOnFailure = false, retriesAfterWake = 0)
    }

    fun togglePower() {
        if (connectionState == ConnectionState.CONNECTED) {
            sendKey("KEY_POWER")
        } else {
            wakeTv()
        }
    }

    private fun wakeTv() {
        val cleanMac = normalizeMacAddress(macAddress) ?: return
        val targetIp = ipAddress.trim()
        val identity = tvPreferences.getLastConnectedIdentity()
        tvPreferences.saveMacAddress(cleanMac, targetIp, identity)
        macAddress = cleanMac

        viewModelScope.launch {
            connectionState = ConnectionState.CONNECTING
            runCatching {
                withContext(Dispatchers.IO) {
                    wakeOnLanSender.send(cleanMac, targetIp.takeIf { it.isNotBlank() })
                }
            }.onFailure {
                connectionState = ConnectionState.FAILED
                return@launch
            }

            if (targetIp.isNotBlank()) {
                delay(WAKE_INITIAL_RECONNECT_DELAY_MS)
                connectToTv(targetIp, identity, wakeOnFailure = false, retriesAfterWake = WAKE_RECONNECT_RETRIES)
            } else {
                connectionState = ConnectionState.DISCONNECTED
            }
        }
    }

    private fun wakeAndReconnect(attempt: Int, targetIp: String, identity: String?) {
        val cleanMac = normalizeMacAddress(macAddress) ?: return
        tvPreferences.saveMacAddress(cleanMac, targetIp, identity)
        macAddress = cleanMac

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    wakeOnLanSender.send(cleanMac, targetIp)
                }
            }.onFailure {
                if (attempt == connectionAttempt) {
                    connectionState = ConnectionState.FAILED
                }
                return@launch
            }

            delay(WAKE_INITIAL_RECONNECT_DELAY_MS)
            if (attempt == connectionAttempt && connectionState == ConnectionState.CONNECTING) {
                connectToTv(
                    targetIp,
                    identity,
                    wakeOnFailure = false,
                    retriesAfterWake = WAKE_RECONNECT_RETRIES
                )
            }
        }
    }

    private fun retryConnectAfterWake(attempt: Int, targetIp: String, identity: String?, retriesLeft: Int) {
        viewModelScope.launch {
            delay(WAKE_RECONNECT_RETRY_DELAY_MS)
            if (attempt == connectionAttempt && connectionState == ConnectionState.CONNECTING) {
                connectToTv(
                    targetIp,
                    identity,
                    wakeOnFailure = false,
                    retriesAfterWake = retriesLeft
                )
            }
        }
    }

    fun toggleFavoriteApp(appId: String) {
        val updated = if (appId in favoriteAppIds) favoriteAppIds - appId else favoriteAppIds + appId
        favoriteAppIds = updated
        appSettings.favoriteAppIds = updated
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        hapticsEnabled = enabled
        appSettings.hapticsEnabled = enabled
    }

    fun updateBackgroundControlEnabled(enabled: Boolean) {
        backgroundControlEnabled = enabled
        appSettings.backgroundControlEnabled = enabled
        if (connectionState == ConnectionState.CONNECTED) {
            if (enabled) startConnectionService() else stopConnectionService()
        }
    }

    fun updateVolumeKeysEnabled(enabled: Boolean) {
        volumeKeysEnabled = enabled
        appSettings.volumeKeysEnabled = enabled
    }

    fun updateIdleTimeoutMinutes(minutes: Int) {
        idleTimeoutMinutes = minutes
        appSettings.idleTimeoutMinutes = minutes
        lastInteractionAt = System.currentTimeMillis()
    }

    private var lastInteractionAt = System.currentTimeMillis()
    private var idleJob: Job? = null

    /** Closes the connection after [idleTimeoutMinutes] without any command sent. */
    private fun startIdleWatch() {
        idleJob?.cancel()
        lastInteractionAt = System.currentTimeMillis()
        idleJob = viewModelScope.launch {
            while (true) {
                delay(IDLE_CHECK_INTERVAL_MS)
                val limitMs = idleTimeoutMinutes * 60_000L
                if (limitMs > 0 && System.currentTimeMillis() - lastInteractionAt >= limitMs) {
                    disconnect()
                    return@launch
                }
            }
        }
    }

    fun sendKey(key: String) {
        lastInteractionAt = System.currentTimeMillis()
        controller?.sendKey(key)
    }

    /** "Wake the TV, then do X": runs at once when connected, otherwise after the TV is back. */
    fun runScene(action: QuickAction) {
        if (connectionState == ConnectionState.CONNECTED) {
            perform(action)
            return
        }
        pendingScene = action
        if (normalizeMacAddress(macAddress) != null) wakeTv() else reconnect()
    }

    private var pendingScene: QuickAction? = null

    private fun perform(action: QuickAction) {
        when (action) {
            QuickAction.Power -> togglePower()
            is QuickAction.Key -> sendKey(action.key)
            is QuickAction.WakeAndKey -> sendKey(action.key)
            is QuickAction.WakeAndApp -> launchApp(action.appId)
        }
    }

    fun sendText(text: String) {
        lastInteractionAt = System.currentTimeMillis()
        controller?.sendText(text)
    }

    fun refreshInstalledApps() {
        controller?.requestInstalledApps()
    }

    fun launchApp(appId: String) {
        lastInteractionAt = System.currentTimeMillis()
        controller?.launchApp(appId)
    }

    private fun saveResolvedMacAddress(ip: String, identity: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val info = samsungDeviceInfoResolver.fetchDeviceInfo(ip)
            val resolvedMac = info?.macAddress?.takeIf { it.isNotBlank() }

            launch(Dispatchers.Main.immediate) {
                tvDeviceInfo = info
                info?.let {
                    val friendly = it.name.takeIf { n -> n != "Smart TV" } ?: it.modelName
                    tvPreferences.upsertSavedTv(ip, identity, friendly)
                    refreshSavedTvs()
                }
                if (!resolvedMac.isNullOrBlank()) {
                    macAddress = resolvedMac
                }
            }

            if (!resolvedMac.isNullOrBlank()) {
                Log.d("RemoteViewModel", "Saving resolved MAC $resolvedMac for TV $ip")
                tvPreferences.saveMacAddress(resolvedMac, ip, identity)
            }
        }
    }

    fun disconnect() {
        pendingScene = null
        idleJob?.cancel()
        idleJob = null
        connectionAttempt++
        controller?.disconnect()
        controller = null
        connectionState = ConnectionState.DISCONNECTED
        stopConnectionService()
    }

    private fun startConnectionService() {
        if (!backgroundControlEnabled) return
        val label = tvNickname.takeIf { it.isNotBlank() } ?: ipAddress
        // Android 12+ refuses to start a foreground service from the background (e.g. a
        // network-triggered reconnect). The connection still works; we just skip the keep-alive.
        runCatching { TvConnectionService.start(getApplication<Application>(), label) }
            .onFailure { Log.w("RemoteViewModel", "Unable to start connection service", it) }
    }

    private fun stopConnectionService() {
        TvConnectionService.stop(getApplication<Application>())
    }

    override fun onCleared() {
        networkCallback?.let { callback ->
            runCatching { connectivityManager?.unregisterNetworkCallback(callback) }
        }
        networkCallback = null
        disconnectReceiver?.let { receiver ->
            runCatching { getApplication<Application>().unregisterReceiver(receiver) }
        }
        disconnectReceiver = null
        disconnect()
    }

    private companion object {
        const val WAKE_INITIAL_RECONNECT_DELAY_MS = 8_000L
        const val WAKE_RECONNECT_RETRY_DELAY_MS = 6_000L
        const val WAKE_RECONNECT_RETRIES = 5
        const val MIN_SEARCH_DURATION_MS = 1_200L
        const val SCENE_SETTLE_MS = 1_500L
        const val IDLE_CHECK_INTERVAL_MS = 30_000L
    }
}
