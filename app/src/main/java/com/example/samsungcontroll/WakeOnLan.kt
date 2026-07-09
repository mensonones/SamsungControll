package com.example.samsungcontroll

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class WakeOnLanSender(private val context: Context) {
    fun send(macAddress: String, targetIp: String? = null) {
        val macBytes = parseMacAddress(macAddress) ?: return
        val payload = ByteArray(6 + 16 * macBytes.size) { index ->
            if (index < 6) {
                0xFF.toByte()
            } else {
                macBytes[(index - 6) % macBytes.size]
            }
        }

        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lock = wifi.createMulticastLock("SamsungTvWakeOnLan")

        DatagramSocket().use { socket ->
            try {
                lock.acquire()
                socket.broadcast = true
                repeat(WOL_REPEAT_COUNT) {
                    broadcastAddresses(targetIp, wifi).forEach { broadcastAddress ->
                        WOL_PORTS.forEach { port ->
                            Log.d("WakeOnLan", "Sending WOL to ${broadcastAddress.hostAddress}:$port for $macAddress")
                            val packet = DatagramPacket(payload, payload.size, broadcastAddress, port)
                            socket.send(packet)
                        }
                    }
                }
            } finally {
                if (lock.isHeld) {
                    lock.release()
                }
            }
        }
    }

    companion object {
        private val WOL_PORTS = intArrayOf(9, 7)
        private const val WOL_REPEAT_COUNT = 3
    }
}

private fun broadcastAddresses(targetIp: String?, wifi: WifiManager): List<InetAddress> {
    return listOfNotNull(
        InetAddress.getByName("255.255.255.255"),
        wifi.dhcpInfo?.broadcastAddress()?.let { InetAddress.getByName(it) },
        targetIp?.subnetBroadcastAddress()?.let { InetAddress.getByName(it) }
    ).distinct()
}

private fun android.net.DhcpInfo.broadcastAddress(): String? {
    if (ipAddress == 0 || netmask == 0) return null

    val broadcast = (ipAddress and netmask) or netmask.inv()
    return listOf(
        broadcast and 0xFF,
        broadcast shr 8 and 0xFF,
        broadcast shr 16 and 0xFF,
        broadcast shr 24 and 0xFF
    ).joinToString(".")
}

private fun String.subnetBroadcastAddress(): String? {
    val parts = split(".")
    if (parts.size != 4 || parts.any { it.toIntOrNull() !in 0..255 }) return null

    return "${parts[0]}.${parts[1]}.${parts[2]}.255"
}

fun normalizeMacAddress(value: String): String? {
    val hex = value.filter { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
    if (hex.length != 12) return null

    return hex.chunked(2).joinToString(":") { it.uppercase() }
}

fun isValidMacAddress(value: String): Boolean {
    return normalizeMacAddress(value) != null
}

private fun parseMacAddress(value: String): ByteArray? {
    val normalized = normalizeMacAddress(value) ?: return null
    return normalized.split(":")
        .map { it.toInt(16).toByte() }
        .toByteArray()
}
