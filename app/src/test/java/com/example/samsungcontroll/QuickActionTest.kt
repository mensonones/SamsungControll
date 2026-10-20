package com.example.samsungcontroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickActionTest {
    @Test
    fun roundTripsEveryAction() {
        listOf(
            QuickAction.Power,
            QuickAction.Key("KEY_MUTE"),
            QuickAction.WakeAndKey("KEY_HDMI1"),
            QuickAction.WakeAndApp("3201907018807")
        ).forEach { action ->
            assertEquals(action, QuickAction.decode(action.encode()))
        }
    }

    @Test
    fun keepsColonsInTheArgument() {
        assertEquals(QuickAction.WakeAndApp("a:b"), QuickAction.decode("wake_app:a:b"))
    }

    @Test
    fun rejectsMalformedValues() {
        assertNull(QuickAction.decode(null))
        assertNull(QuickAction.decode(""))
        assertNull(QuickAction.decode("key"))
        assertNull(QuickAction.decode("key:"))
        assertNull(QuickAction.decode("unknown:KEY_UP"))
    }
}
