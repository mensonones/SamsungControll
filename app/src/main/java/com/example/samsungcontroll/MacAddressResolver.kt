package com.example.samsungcontroll

import android.util.Log
import java.io.File

class MacAddressResolver {
    fun resolve(ip: String): String? {
        if (!isLocalNetworkHost(ip)) return null

        return runCatching {
            File(ARP_CACHE_PATH)
                .takeIf { it.canRead() }
                ?.useLines { lines ->
                    lines.drop(1).firstNotNullOfOrNull { line ->
                        val columns = line.trim().split(Regex("""\s+"""))
                        val candidateIp = columns.getOrNull(0)
                        val candidateMac = columns.getOrNull(3)

                        if (candidateIp == ip && candidateMac != null && candidateMac != EMPTY_MAC) {
                            normalizeMacAddress(candidateMac)
                        } else {
                            null
                        }
                    }
                }
        }.onFailure { error ->
            Log.w("MacAddressResolver", "Unable to resolve MAC address for $ip", error)
        }.getOrNull()
    }

    private companion object {
        const val ARP_CACHE_PATH = "/proc/net/arp"
        const val EMPTY_MAC = "00:00:00:00:00:00"
    }
}
