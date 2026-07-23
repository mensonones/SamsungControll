package com.example.samsungcontroll.di

import com.example.samsungcontroll.CertificatePinStore
import com.example.samsungcontroll.DiscoveryService
import com.example.samsungcontroll.MacAddressResolver
import com.example.samsungcontroll.RemoteViewModel
import com.example.samsungcontroll.SamsungDeviceInfoResolver
import com.example.samsungcontroll.SecureTvPreferences
import com.example.samsungcontroll.TvDiscovery
import com.example.samsungcontroll.WakeOnLanSender
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SecureTvPreferences(androidContext()) }
    single<CertificatePinStore> { get<SecureTvPreferences>() }
    single { MacAddressResolver() }
    single { SamsungDeviceInfoResolver() }
    single { WakeOnLanSender(androidContext()) }
    single<DiscoveryService> { TvDiscovery(androidContext()) }

    viewModel {
        RemoteViewModel(
            application = androidApplication(),
            tvPreferences = get(),
            discoveryService = get(),
            macAddressResolver = get(),
            samsungDeviceInfoResolver = get(),
            wakeOnLanSender = get()
        )
    }
}
