package com.example.samsungcontroll

import java.net.InetAddress

private val IPV4_PATTERN = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
private val IPV6_PATTERN = Regex("""^[0-9a-fA-F:]+$""")

fun isLocalNetworkHost(host: String): Boolean {
    val trimmedHost = host.trim()
    if (!IPV4_PATTERN.matches(trimmedHost) && !IPV6_PATTERN.matches(trimmedHost)) {
        return false
    }

    return runCatching {
        InetAddress.getByName(trimmedHost).let { address ->
            address.isSiteLocalAddress || address.isLinkLocalAddress || address.isLoopbackAddress
        }
    }.getOrDefault(false)
}
