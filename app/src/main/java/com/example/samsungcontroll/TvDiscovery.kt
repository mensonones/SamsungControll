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

data class DiscoveredTv(val name: String, val ip: String)

class TvDiscovery(private val context: Context) : DiscoveryService {
    private val ssdpMulticastAddress = "239.255.255.250"
    private val ssdpPort = 1900
    
    private val searchTargets = listOf(
        "urn:samsung.com:device:RemoteControlReceiver:1",
        "upnp:rootdevice",
        "ssdp:all"
    )

    override suspend fun discoverTvs(): List<DiscoveredTv> = withContext(Dispatchers.IO) {
        val discovered = mutableSetOf<DiscoveredTv>()
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

                            if (ip != null && isLocalNetworkHost(ip)) {
                                if (response.contains("Samsung", ignoreCase = true) || target.contains("samsung")) {
                                    discovered.add(DiscoveredTv("Samsung TV ($ip)", ip))
                                } else if (target == "upnp:rootdevice") {
                                    discovered.add(DiscoveredTv("Dispositivo encontrado ($ip)", ip))
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

        discovered.toList()
    }
}
