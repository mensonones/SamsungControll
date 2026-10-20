package com.example.samsungcontroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiscoveryParsingTest {
    @Test
    fun extractsLowercasedUuidFromUsn() {
        val response = "HTTP/1.1 200 OK\r\nUSN: uuid:ABCD-1234::urn:samsung.com:device:RemoteControlReceiver:1\r\n\r\n"
        assertEquals("uuid:abcd-1234", response.extractSsdpIdentity())
    }

    @Test
    fun returnsNullWithoutUsn() {
        assertNull("HTTP/1.1 200 OK\r\nST: ssdp:all\r\n\r\n".extractSsdpIdentity())
    }

    @Test
    fun mergeReplacesGenericNameAndKeepsMac() {
        val tvs = linkedMapOf<String, DiscoveredTv>()
        tvs.mergeTv(DiscoveredTv("Samsung Smart TV", "192.168.1.18", "uuid:a", "AA:BB:CC:00:11:22"))
        tvs.mergeTv(DiscoveredTv("Samsung AU7700 43 TV", "192.168.1.18", "uuid:a", null))

        val merged = tvs.values.single()
        assertEquals("Samsung AU7700 43 TV", merged.name)
        assertEquals("AA:BB:CC:00:11:22", merged.macAddress)
    }

    @Test
    fun mergeJoinsEntriesByIdentityAcrossIps() {
        val tvs = linkedMapOf<String, DiscoveredTv>()
        tvs.mergeTv(DiscoveredTv("Samsung Smart TV", "192.168.1.18"))
        tvs.mergeTv(DiscoveredTv("Samsung Smart TV", "192.168.1.18", "uuid:a"))

        assertEquals(1, tvs.size)
        assertEquals("uuid:a", tvs.values.single().identity)
    }

    @Test
    fun subnetBroadcastReplacesLastOctet() {
        assertEquals("192.168.1.255", "192.168.1.18".subnetBroadcastAddress())
        assertNull("not-an-ip".subnetBroadcastAddress())
        assertNull("192.168.1.300".subnetBroadcastAddress())
    }
}
