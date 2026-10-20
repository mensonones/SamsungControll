package com.example.samsungcontroll

/** An app installed on the TV, as reported by the `ed.installedApp.get` event. */
data class TvApp(val appId: String, val name: String)

interface TvController {
    fun connect()
    fun disconnect()
    fun isConnected(): Boolean
    fun sendKey(key: String)
    fun sendText(text: String)
    fun launchApp(appId: String)
    fun requestInstalledApps()
}
