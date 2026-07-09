package com.example.samsungcontroll

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val discoveryService: DiscoveryService = TvDiscovery(application)
    private val tvPreferences = SecureTvPreferences(application)
    private val wakeOnLanSender = WakeOnLanSender(application)
    private val macAddressResolver = MacAddressResolver()
    private val samsungDeviceInfoResolver = SamsungDeviceInfoResolver()

    var ipAddress by mutableStateOf("")
        private set

    var macAddress by mutableStateOf("")
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
    
    private var controller: TvController? = null
    private var connectionAttempt = 0

    fun initialize() {
        val lastIp = tvPreferences.getLastConnectedIp()
        val lastIdentity = tvPreferences.getLastConnectedIdentity()
        macAddress = tvPreferences.getMacAddress(lastIp, lastIdentity)
        if (lastIp.isNotBlank() && connectionState == ConnectionState.DISCONNECTED) {
            ipAddress = lastIp
            connectToLastTv(lastIp, lastIdentity)
        }
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
            isSearching = true
            discoveredTvs = discoveryService.discoverTvs()
            isSearching = false
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
        retriesAfterWake: Int
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
        controller?.disconnect()
        ipAddress = cleanIp
        
        controller = SamsungTvController(
            tvIp = cleanIp,
            token = savedToken,
            certificatePinStore = tvPreferences,
            onTokenReceived = { newToken ->
                tvPreferences.saveToken(cleanIp, savedIdentity, newToken)
            },
            onStateChange = { newState ->
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    if (currentAttempt == connectionAttempt) {
                        if (newState == ConnectionState.FAILED && wakeOnFailure && macAddress.isNotBlank()) {
                            connectionState = ConnectionState.CONNECTING
                            wakeAndReconnect(currentAttempt, cleanIp, savedIdentity)
                        } else if (newState == ConnectionState.FAILED && retriesAfterWake > 0) {
                            connectionState = ConnectionState.CONNECTING
                            retryConnectAfterWake(currentAttempt, cleanIp, savedIdentity, retriesAfterWake - 1)
                        } else {
                            connectionState = newState
                            if (newState == ConnectionState.CONNECTED) {
                                showDiscovery = false
                                saveResolvedMacAddress(cleanIp, savedIdentity)
                            }
                        }
                    }
                }
            }
        ).apply { connect() }
    }

    fun reconnect() {
        if (ipAddress.isNotBlank()) {
            connectToLastTv(ipAddress, tvPreferences.getLastConnectedIdentity())
        }
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

    private fun connectToLastTv(targetIp: String, identity: String?) {
        val cleanMac = normalizeMacAddress(macAddress)
        if (cleanMac == null) {
            Log.w("RemoteViewModel", "No valid MAC saved for $targetIp; connecting without WOL")
            connectToTv(targetIp, identity, wakeOnFailure = false, retriesAfterWake = 0)
            return
        }

        tvPreferences.saveMacAddress(cleanMac, targetIp, identity)
        macAddress = cleanMac
        val attempt = ++connectionAttempt
        connectionState = ConnectionState.CONNECTING

        viewModelScope.launch {
            Log.d("RemoteViewModel", "Sending WOL before connecting to $targetIp with MAC $cleanMac")
            runCatching {
                withContext(Dispatchers.IO) {
                    wakeOnLanSender.send(cleanMac, targetIp)
                }
            }.onFailure { error ->
                Log.w("RemoteViewModel", "Unable to send WOL to $targetIp", error)
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

    fun sendKey(key: String) {
        controller?.sendKey(key)
    }

    fun launchApp(appId: String) {
        controller?.launchApp(appId)
    }

    private fun saveResolvedMacAddress(ip: String, identity: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolvedMac = samsungDeviceInfoResolver.resolveMacAddress(ip)
                ?: macAddressResolver.resolve(ip)
                ?: run {
                    Log.w("RemoteViewModel", "Unable to resolve MAC for connected TV $ip")
                    return@launch
                }

            Log.d("RemoteViewModel", "Saving resolved MAC $resolvedMac for TV $ip")
            tvPreferences.saveMacAddress(resolvedMac, ip, identity)
            launch(Dispatchers.Main.immediate) {
                macAddress = resolvedMac
            }
        }
    }

    fun disconnect() {
        connectionAttempt++
        controller?.disconnect()
        controller = null
        connectionState = ConnectionState.DISCONNECTED
    }

    override fun onCleared() {
        disconnect()
    }

    private companion object {
        const val WAKE_INITIAL_RECONNECT_DELAY_MS = 8_000L
        const val WAKE_RECONNECT_RETRY_DELAY_MS = 6_000L
        const val WAKE_RECONNECT_RETRIES = 5
    }
}
