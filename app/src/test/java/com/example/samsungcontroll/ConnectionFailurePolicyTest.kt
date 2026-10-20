package com.example.samsungcontroll

import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionFailurePolicyTest {
    @Test
    fun neverWakesTheTvAfterAnEstablishedConnectionDrops() {
        // Regression: the TV was switched off while connected, the dropped socket reported
        // FAILED and the stale wakeOnFailure flag sent Wake-on-LAN, turning the TV back on.
        assertEquals(
            FailureAction.SURFACE,
            failureAction(connectedOnce = true, wakeOnFailure = true, hasMacAddress = true, retriesAfterWake = 0)
        )
        assertEquals(
            FailureAction.SURFACE,
            failureAction(connectedOnce = true, wakeOnFailure = false, hasMacAddress = true, retriesAfterWake = 5)
        )
    }

    @Test
    fun wakesWhenTheFirstAttemptFailsAndAMacIsKnown() {
        assertEquals(
            FailureAction.WAKE_AND_RECONNECT,
            failureAction(connectedOnce = false, wakeOnFailure = true, hasMacAddress = true, retriesAfterWake = 0)
        )
    }

    @Test
    fun retriesWhileWakingWithoutSendingAnotherPacket() {
        assertEquals(
            FailureAction.RETRY,
            failureAction(connectedOnce = false, wakeOnFailure = false, hasMacAddress = true, retriesAfterWake = 3)
        )
    }

    @Test
    fun surfacesTheFailureWithoutMacOrRetries() {
        assertEquals(
            FailureAction.SURFACE,
            failureAction(connectedOnce = false, wakeOnFailure = true, hasMacAddress = false, retriesAfterWake = 0)
        )
    }
}
