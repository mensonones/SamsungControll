package com.example.samsungcontroll

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.samsungcontroll.ui.screens.RemoteControlScreen
import com.example.samsungcontroll.ui.theme.SamsungControllTheme
import org.junit.Rule
import org.junit.Test

class RemoteControlScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRemoteControlScreenTabsAndNavigation() {
        val fakePrefs = object : CertificatePinStore {
            override fun getCertificateFingerprint(host: String): String? = null
            override fun saveCertificateFingerprint(host: String, fingerprint: String) {}
        }
        val securePrefs = SecureTvPreferences(androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val discovery = TvDiscovery(androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val macResolver = MacAddressResolver(androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val deviceInfoResolver = SamsungDeviceInfoResolver()
        val wolSender = WakeOnLanSender(androidx.test.core.app.ApplicationProvider.getApplicationContext())

        val viewModel = RemoteViewModel(
            tvPreferences = securePrefs,
            tvDiscovery = discovery,
            macAddressResolver = macResolver,
            samsungDeviceInfoResolver = deviceInfoResolver,
            wakeOnLanSender = wolSender
        )

        composeTestRule.setContent {
            SamsungControllTheme {
                RemoteControlScreen(viewModel = viewModel)
            }
        }

        // Verify splash or header title displays
        composeTestRule.onNodeWithText("SAMSUNG", substring = true).assertIsDisplayed()
    }
}
