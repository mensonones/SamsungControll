package com.example.samsungcontroll

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

enum class ConnectionState {
    DISCONNECTED, CONNECTING, WAITING_FOR_PERMISSION, CONNECTED, FAILED
}

class SamsungTvController(
    private val tvIp: String,
    private val appName: String = "SamsungRemote",
    private var token: String? = null,
    private val certificatePinStore: CertificatePinStore,
    private val onTokenReceived: (String) -> Unit = {},
    private val onStateChange: (ConnectionState) -> Unit
) : TvController {
    private var webSocket: WebSocket? = null

    private val client: OkHttpClient by lazy {
        createHttpClient()
    }

    override fun connect() {
        if (!isLocalNetworkHost(tvIp)) {
            onStateChange(ConnectionState.FAILED)
            return
        }
        connectInternal(port = 8002, useSsl = true)
    }

    private fun connectInternal(port: Int, useSsl: Boolean) {
        onStateChange(ConnectionState.CONNECTING)
        val hasSavedToken = !token.isNullOrBlank()
        val encodedName = Base64.encodeToString(appName.toByteArray(), Base64.NO_WRAP)
        val httpScheme = if (useSsl) "https" else "http"
        val webSocketScheme = if (useSsl) "wss" else "ws"

        val httpUrl = HttpUrl.Builder()
            .scheme(httpScheme)
            .host(tvIp)
            .port(port)
            .addPathSegments("api/v2/channels/samsung.remote.control")
            .addQueryParameter("name", encodedName)
            .apply {
                if (!token.isNullOrBlank()) {
                    addQueryParameter("token", token)
                }
            }
            .build()
        val webSocketUrl = httpUrl.toString().replaceFirst("$httpScheme://", "$webSocketScheme://")

        Log.d("SamsungTv", "Connecting to Samsung TV on port $port")
        val request = Request.Builder().url(webSocketUrl).build()

        webSocket?.cancel()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("SamsungTv", "WebSocket opened on port $port")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    val json = JSONObject(text)
                    json.optJSONObject("data")?.optString("token")?.takeIf { it.isNotBlank() }?.let { newToken ->
                        if (newToken != token) {
                            token = newToken
                            onTokenReceived(newToken)
                        }
                    }
                    when (json.optString("event")) {
                        "ms.channel.connect" -> onStateChange(ConnectionState.CONNECTED)
                        "ms.channel.unauthorized" -> onStateChange(ConnectionState.WAITING_FOR_PERMISSION)
                    }
                }.onFailure { error ->
                    Log.w("SamsungTv", "Ignoring malformed TV message", error)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                response?.close()
                if (port == 8002 && !hasSavedToken) {
                    connectInternal(8001, false)
                } else {
                    Log.w("SamsungTv", "Unable to connect to Samsung TV", t)
                    onStateChange(ConnectionState.FAILED)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onStateChange(ConnectionState.DISCONNECTED)
            }
        })
    }

    @SuppressLint("CustomX509TrustManager")
    private fun createHttpClient(): OkHttpClient {
        val trustManager = PinnedCertificateTrustManager(tvIp, certificatePinStore)
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { hostname, _ ->
                hostname == tvIp && isLocalNetworkHost(hostname)
            }
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    override fun sendKey(key: String) {
        val json = JSONObject().apply {
            put("method", "ms.remote.control")
            put("params", JSONObject().apply {
                put("Cmd", "Click")
                put("DataOfCmd", key)
                put("Option", "false")
                put("TypeOfRemote", "SendRemoteKey")
            })
        }
        webSocket?.send(json.toString())
    }

    override fun launchApp(appId: String) {
        if (!isLocalNetworkHost(tvIp)) {
            onStateChange(ConnectionState.FAILED)
            return
        }

        val restUrl = HttpUrl.Builder()
            .scheme("http")
            .host(tvIp)
            .port(8001)
            .addPathSegments("api/v2/applications")
            .addPathSegment(appId)
            .build()

        val request = Request.Builder()
            .url(restUrl)
            .post(ByteArray(0).toRequestBody(null))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.w("SamsungTv", "REST app launch failed, trying WebSocket fallback", e)
                launchAppWebSocket(appId)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        launchAppWebSocket(appId)
                    }
                }
            }
        })
    }

    private fun launchAppWebSocket(appId: String) {
        val json = JSONObject().apply {
            put("method", "ms.channel.emit")
            put("params", JSONObject().apply {
                put("event", "ed.apps.launch")
                put("to", "host")
                put("data", JSONObject().apply {
                    put("appId", appId)
                    put("action_type", "DEEP_LINK")
                })
            })
        }
        webSocket?.send(json.toString())
    }

    override fun disconnect() {
        webSocket?.close(1000, "User requested")
        webSocket = null
        onStateChange(ConnectionState.DISCONNECTED)
    }
}

@SuppressLint("CustomX509TrustManager")
private class PinnedCertificateTrustManager(
    private val host: String,
    private val certificatePinStore: CertificatePinStore
) : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        throw CertificateException("Client certificates are not supported")
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        val leafCertificate = chain?.firstOrNull() ?: throw CertificateException("Missing server certificate")
        val fingerprint = leafCertificate.sha256Fingerprint()
        val pinnedFingerprint = certificatePinStore.getCertificateFingerprint(host)

        when {
            pinnedFingerprint == null -> certificatePinStore.saveCertificateFingerprint(host, fingerprint)
            !pinnedFingerprint.equals(fingerprint, ignoreCase = true) -> {
                throw CertificateException("Samsung TV certificate changed for $host")
            }
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}

private fun X509Certificate.sha256Fingerprint(): String {
    val digest = java.security.MessageDigest.getInstance("SHA-256").digest(encoded)
    return digest.joinToString(separator = ":") { byte -> "%02X".format(byte) }
}
