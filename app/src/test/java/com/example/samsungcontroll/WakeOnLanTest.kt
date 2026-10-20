package com.example.samsungcontroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WakeOnLanTest {
    @Test
    fun normalizesMacWithDifferentSeparators() {
        assertEquals("AA:BB:CC:00:11:22", normalizeMacAddress("aa-bb-cc-00-11-22"))
        assertEquals("AA:BB:CC:00:11:22", normalizeMacAddress("aabb.cc00.1122"))
    }

    @Test
    fun rejectsInvalidMac() {
        assertNull(normalizeMacAddress("aa:bb:cc"))
        assertFalse(isValidMacAddress(""))
        assertTrue(isValidMacAddress("AA:BB:CC:00:11:22"))
    }
}
