package com.example.samsungcontroll

interface TvController {
    fun connect()
    fun disconnect()
    fun sendKey(key: String)
    fun launchApp(appId: String)
}
