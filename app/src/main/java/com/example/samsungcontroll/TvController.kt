package com.example.samsungcontroll

interface TvController {
    fun connect()
    fun disconnect()
    fun isConnected(): Boolean
    fun sendKey(key: String)
    fun launchApp(appId: String)
}
