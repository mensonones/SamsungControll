package com.example.samsungcontroll

import com.example.samsungcontroll.ui.haptics.HapticsManager
import com.example.samsungcontroll.ui.haptics.NoOpHapticsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HapticsManagerTest {

    private class MockHapticsManager : HapticsManager {
        var clickCount = 0
        var keypressCount = 0
        var toggleCount = 0

        override fun performClick() {
            clickCount++
        }

        override fun performKeypress() {
            keypressCount++
        }

        override fun performToggle() {
            toggleCount++
        }
    }

    @Test
    fun testNoOpHapticsManagerDoesNotThrow() {
        val noOp = NoOpHapticsManager()
        noOp.performClick()
        noOp.performKeypress()
        noOp.performToggle()
        assertTrue("NoOpHapticsManager executed without throwing", true)
    }

    @Test
    fun testMockHapticsManagerContract() {
        val mock: HapticsManager = MockHapticsManager()
        mock.performClick()
        mock.performKeypress()
        mock.performToggle()

        val mockImpl = mock as MockHapticsManager
        assertEquals(1, mockImpl.clickCount)
        assertEquals(1, mockImpl.keypressCount)
        assertEquals(1, mockImpl.toggleCount)
    }
}
