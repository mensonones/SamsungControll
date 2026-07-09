package com.example.samsungcontroll

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SamsungDeviceInfoResolver {
    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .build()

    fun resolveMacAddress(ip: String): String? {
        if (!isLocalNetworkHost(ip)) return null

        return runCatching {
            val request = Request.Builder()
                .url("http://$ip:8001/api/v2/")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val body = response.body?.string().orEmpty()
                findMacAddress(JSONObject(body))
            }
        }.onFailure { error ->
            Log.w("SamsungDeviceInfoResolver", "Unable to resolve TV MAC from API for $ip", error)
        }.getOrNull()
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
