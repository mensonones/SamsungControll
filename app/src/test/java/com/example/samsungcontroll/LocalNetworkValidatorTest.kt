package com.example.samsungcontroll

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalNetworkValidatorTest {
    @Test
    fun acceptsPrivateAddresses() {
        assertTrue(isLocalNetworkHost("192.168.0.10"))
        assertTrue(isLocalNetworkHost("10.0.0.2"))
        assertTrue(isLocalNetworkHost(" 172.16.5.5 "))
    }

    @Test
    fun rejectsPublicAddressesAndHostnames() {
        assertFalse(isLocalNetworkHost("8.8.8.8"))
        assertFalse(isLocalNetworkHost("example.com"))
        assertFalse(isLocalNetworkHost(""))
    }
}
