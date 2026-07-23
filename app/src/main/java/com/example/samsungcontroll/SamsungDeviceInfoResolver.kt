package com.example.samsungcontroll

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TvDeviceInfo(
    val modelName: String = "Samsung Smart TV",
    val name: String = "Smart TV",
    val os: String = "Tizen OS",
    val networkType: String = "Wi-Fi / Rede",
    val macAddress: String = "",
    val firmwareVersion: String = ""
)

class SamsungDeviceInfoResolver {
    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .build()

    fun fetchDeviceInfo(ip: String): TvDeviceInfo? {
        if (!isLocalNetworkHost(ip)) return null

        return runCatching {
            val request = Request.Builder()
                .url("http://$ip:8001/api/v2/")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val body = response.body.string()
                val json = JSONObject(body)
                val deviceObj = json.optJSONObject("device")

                val model = deviceObj?.optString("modelName")?.takeIf { it.isNotBlank() }
                    ?: deviceObj?.optString("model")?.takeIf { it.isNotBlank() }
                    ?: "Samsung Smart TV"
                val name = deviceObj?.optString("name")?.takeIf { it.isNotBlank() } ?: "Smart TV"
                val os = deviceObj?.optString("OS")?.takeIf { it.isNotBlank() } ?: "Tizen OS"
                val netType = deviceObj?.optString("networkType")?.takeIf { it.isNotBlank() } ?: "Wi-Fi"
                val mac = findMacAddress(json).orEmpty()
                val firmware = deviceObj?.optString("firmwareVersion")?.takeIf { it.isNotBlank() } ?: ""

                TvDeviceInfo(
                    modelName = model,
                    name = name,
                    os = os,
                    networkType = netType,
                    macAddress = mac,
                    firmwareVersion = firmware
                )
            }
        }.onFailure { error ->
            Log.w("SamsungDeviceInfoResolver", "Unable to fetch TV device info for $ip", error)
        }.getOrNull()
    }

    fun resolveMacAddress(ip: String): String? {
        return fetchDeviceInfo(ip)?.macAddress?.takeIf { it.isNotBlank() }
    }

    private fun findMacAddress(value: Any?): String? {
        return when (value) {
            is JSONObject -> {
                value.keys().asSequence().firstNotNullOfOrNull { key ->
                    val child = value.opt(key)
                    if (key.contains("mac", ignoreCase = true) && child is String) {
                        normalizeMacAddress(child)
                    } else {
                        findMacAddress(child)
                    }
                }
            }
            is JSONArray -> {
                (0 until value.length()).firstNotNullOfOrNull { index ->
                    findMacAddress(value.opt(index))
                }
            }
            is String -> normalizeMacAddress(value)
            else -> null
        }
    }
}
