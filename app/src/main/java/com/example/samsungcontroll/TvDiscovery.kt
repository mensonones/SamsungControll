package com.example.samsungcontroll

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.coroutines.coroutineContext

data class DiscoveredTv(
    val name: String,
    val ip: String,
    val identity: String? = null,
    val macAddress: String? = null
)

class TvDiscovery(private val context: Context) : DiscoveryService {
    private val ssdpMulticastAddress = "239.255.255.250"
    private val ssdpPort = 1900
    private val macAddressResolver = MacAddressResolver()
    
    private val searchTargets = listOf(
        "urn:samsung.com:device:RemoteControlReceiver:1",
        "upnp:rootdevice",
        "ssdp:all"
    )

    override suspend fun discoverTvs(): List<DiscoveredTv> = withContext(Dispatchers.IO) {
        val discovered = linkedMapOf<String, DiscoveredTv>()
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lock = wifi.createMulticastLock("SamsungTvDiscovery")
        
        try {
            lock.acquire()
            DatagramSocket().use { socket ->
                socket.soTimeout = 1500

                for (target in searchTargets) {
                    coroutineContext.ensureActive()
                    val query = """
                        M-SEARCH * HTTP/1.1
                        HOST: $ssdpMulticastAddress:$ssdpPort
                        MAN: "ssdp:discover"
                        MX: 2
                        ST: $target

                    """.trimIndent().replace("\n", "\r\n") + "\r\n"

                    val group = InetAddress.getByName(ssdpMulticastAddress)
                    val queryBytes = query.toByteArray(Charsets.UTF_8)
                    val packet = DatagramPacket(queryBytes, queryBytes.size, group, ssdpPort)
                    socket.send(packet)

                    val buffer = ByteArray(2048)
                    val startTime = System.currentTimeMillis()
                    while (System.currentTimeMillis() - startTime < 2000) {
                        coroutineContext.ensureActive()
                        try {
                            val responsePacket = DatagramPacket(buffer, buffer.size)
                            socket.receive(responsePacket)
                            val response = String(responsePacket.data, 0, responsePacket.length, Charsets.UTF_8)
                            val ip = responsePacket.address.hostAddress
                            val identity = response.extractSsdpIdentity()

                            if (ip != null && isLocalNetworkHost(ip)) {
                                val macAddress = macAddressResolver.resolve(ip)
                                if (response.contains("Samsung", ignoreCase = true) || target.contains("samsung")) {
                                    discovered.mergeTv(DiscoveredTv("Samsung TV ($ip)", ip, identity, macAddress))
                                } else if (target == "upnp:rootdevice") {
                                    discovered.mergeTv(DiscoveredTv("Dispositivo encontrado ($ip)", ip, identity, macAddress))
                                }
                            }
                        } catch (e: SocketTimeoutException) {
                            break
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("TvDiscovery", "Unable to discover TVs", e)
        } finally {
            if (lock.isHeld) {
                lock.release()
            }
        }

        discovered.values.toList()
    }
}

private fun MutableMap<String, DiscoveredTv>.mergeTv(tv: DiscoveredTv) {
    val key = tv.identity ?: tv.ip
    val sameIpKey = entries.firstOrNull { it.value.ip == tv.ip }?.key
    val current = this[key]
        ?: sameIpKey?.let { remove(it) }
        ?: tv.identity?.let { remove(tv.ip) }

    this[key] = when {
        current == null -> tv
        current.isGenericName() && !tv.isGenericName() -> tv.copy(
            macAddress = tv.macAddress ?: current.macAddress
        )
        current.macAddress.isNullOrBlank() && !tv.macAddress.isNullOrBlank() -> current.copy(
            macAddress = tv.macAddress
        )
        current.identity.isNullOrBlank() && !tv.identity.isNullOrBlank() -> current.copy(
            identity = tv.identity
        )
        else -> current
    }
}

private fun DiscoveredTv.isGenericName(): Boolean {
    return name.startsWith("Dispositivo encontrado")
}

private fun String.extractSsdpIdentity(): String? {
    lineSequence().forEach { line ->
        val separatorIndex = line.indexOf(':')
        if (separatorIndex <= 0) return@forEach

        val header = line.substring(0, separatorIndex).trim()
        if (!header.equals("USN", ignoreCase = true)) return@forEach

        val value = line.substring(separatorIndex + 1).trim()
        val uuid = Regex("""uuid:[^:\s]+""", RegexOption.IGNORE_CASE)
            .find(value)
            ?.value
            ?.lowercase()

        if (!uuid.isNullOrBlank()) {
            return uuid
        }
    }

    return null
}
