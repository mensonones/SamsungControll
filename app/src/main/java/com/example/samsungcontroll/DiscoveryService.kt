package com.example.samsungcontroll

interface DiscoveryService {
    suspend fun discoverTvs(): List<DiscoveredTv>
}
