package com.example.samsungcontroll

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface CertificatePinStore {
    fun getCertificateFingerprint(host: String): String?
    fun saveCertificateFingerprint(host: String, fingerprint: String)
}

class SecureTvPreferences(context: Context) : CertificatePinStore {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLastConnectedIp(): String {
        return prefs.getString(KEY_LAST_CONNECTED_IP, "") ?: ""
    }

    fun saveLastConnectedIp(ip: String) {
        prefs.edit { putString(KEY_LAST_CONNECTED_IP, ip) }
    }

    fun getLastConnectedIdentity(): String? {
        return prefs.getString(KEY_LAST_CONNECTED_IDENTITY, null)?.takeIf { it.isNotBlank() }
    }

    fun saveLastConnectedIdentity(identity: String?) {
        prefs.edit {
            if (identity.isNullOrBlank()) {
                remove(KEY_LAST_CONNECTED_IDENTITY)
            } else {
                putString(KEY_LAST_CONNECTED_IDENTITY, identity)
            }
        }
    }

    fun getMacAddress(ip: String? = null, identity: String? = null): String {
        tvAliases(ip, identity).forEach { alias ->
            prefs.getString(macKey(alias), null)?.let { return it }
        }

        return prefs.getString(KEY_LAST_CONNECTED_MAC, "") ?: ""
    }

    fun saveMacAddress(macAddress: String, ip: String? = null, identity: String? = null) {
        val normalizedMac = normalizeMacAddress(macAddress) ?: return
        prefs.edit {
            putString(KEY_LAST_CONNECTED_MAC, normalizedMac)
            tvAliases(ip, identity).forEach { alias ->
                putString(macKey(alias), normalizedMac)
            }
        }
    }

    fun getToken(ip: String, identity: String? = null): String? {
        tokenAliases(ip, identity).forEach { alias ->
            readToken(alias)?.let { token ->
                if (alias != ip) {
                    saveToken(ip, identity, token)
                }
                return token
            }
        }

        val legacyToken = prefs.getString(legacyTokenKey(ip), null)
        if (!legacyToken.isNullOrBlank()) {
            saveToken(ip, identity, legacyToken)
            prefs.edit { remove(legacyTokenKey(ip)) }
        }
        return legacyToken
    }

    fun saveToken(ip: String, identity: String? = null, token: String) {
        prefs.edit {
            tokenAliases(ip, identity).forEach { alias ->
                putString(tokenKey(alias), encrypt(token))
            }
            remove(legacyTokenKey(ip))
        }
    }

    private fun readToken(alias: String): String? {
        val encryptedToken = prefs.getString(tokenKey(alias), null)
        if (encryptedToken != null) {
            return decrypt(encryptedToken)
        }

        return null
    }

    override fun getCertificateFingerprint(host: String): String? {
        return prefs.getString(certificateKey(host), null)
    }

    override fun saveCertificateFingerprint(host: String, fingerprint: String) {
        prefs.edit { putString(certificateKey(host), fingerprint) }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return "${Base64.encodeToString(cipher.iv, Base64.NO_WRAP)}:${
            Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        }"
    }

    private fun decrypt(value: String): String? {
        return runCatching {
            val parts = value.split(":", limit = 2)
            if (parts.size != 2) return null

            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        }.onFailure { error ->
            Log.w("SecureTvPreferences", "Unable to decrypt stored TV token", error)
        }.getOrNull()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
            generateKey()
        }
    }

    private fun tokenAliases(ip: String, identity: String?): List<String> {
        return tvAliases(ip, identity)
    }

    private fun tvAliases(ip: String?, identity: String?): List<String> {
        return listOfNotNull(identity?.takeIf { it.isNotBlank() }, ip?.takeIf { it.isNotBlank() }).distinct()
    }

    private fun tokenKey(alias: String) = "token_enc_$alias"
    private fun legacyTokenKey(ip: String) = "token_$ip"
    private fun certificateKey(host: String) = "cert_sha256_$host"
    private fun macKey(alias: String) = "mac_$alias"

    private companion object {
        const val PREFS_NAME = "tv_prefs"
        const val KEY_ALIAS = "samsung_remote_token_key"
        const val KEY_LAST_CONNECTED_IP = "last_connected_ip"
        const val KEY_LAST_CONNECTED_IDENTITY = "last_connected_identity"
        const val KEY_LAST_CONNECTED_MAC = "last_connected_mac"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
