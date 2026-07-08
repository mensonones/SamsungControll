package com.example.samsungcontroll

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val discoveryService: DiscoveryService = TvDiscovery(application)
    private val tvPreferences = SecureTvPreferences(application)

    var ipAddress by mutableStateOf("")
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
        if (lastIp.isNotBlank() && connectionState == ConnectionState.DISCONNECTED) {
            ipAddress = lastIp
            connectToTv(lastIp)
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

    fun connectToTv(ip: String) {
        val cleanIp = ip.trim()
        if (cleanIp.isBlank()) return

        if (!isLocalNetworkHost(cleanIp)) {
            connectionState = ConnectionState.FAILED
            return
        }

        val currentAttempt = ++connectionAttempt
        val savedToken = tvPreferences.getToken(cleanIp)
        tvPreferences.saveLastConnectedIp(cleanIp)
        controller?.disconnect()
        ipAddress = cleanIp
        
        controller = SamsungTvController(
            tvIp = cleanIp,
            token = savedToken,
            certificatePinStore = tvPreferences,
            onTokenReceived = { newToken ->
                tvPreferences.saveToken(cleanIp, newToken)
            },
            onStateChange = { newState ->
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    if (currentAttempt == connectionAttempt) {
                        connectionState = newState
                        if (newState == ConnectionState.CONNECTED) {
                            showDiscovery = false
                        }
                    }
                }
            }
        ).apply { connect() }
    }

    fun reconnect() {
        if (ipAddress.isNotBlank()) {
            connectToTv(ipAddress)
        }
    }

    fun sendKey(key: String) {
        controller?.sendKey(key)
    }

    fun launchApp(appId: String) {
        controller?.launchApp(appId)
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
}
