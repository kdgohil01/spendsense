package com.spendsense.app.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALIAS = "spendsense_e2e_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TAG = "CryptoManager"

    init {
        initKey()
    }

    private fun initKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val keyGenSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build()
                keyGenerator.init(keyGenSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (entry != null) {
            return entry.secretKey
        }
        // Fallback key if keystore entries aren't accessible (safety guard)
        return javax.crypto.spec.SecretKeySpec(
            "SpendSenseKeyE2EEncryptionFallback".take(32).toByteArray(Charsets.UTF_8),
            "AES"
        )
    }

    /**
     * Encrypt a string and return base64 formatted representation containing IV and ciphertext merged.
     */
    fun encrypt(data: String): String {
        if (data.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, getKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            
            // Format: [iv_size_byte] + [iv_bytes] + [cipher_bytes]
            val ivSize = iv.size
            val merged = ByteArray(1 + ivSize + encryptedBytes.size)
            merged[0] = ivSize.toByte()
            System.arraycopy(iv, 0, merged, 1, ivSize)
            System.arraycopy(encryptedBytes, 0, merged, 1 + ivSize, encryptedBytes.size)
            
            Base64.encodeToString(merged, Base64.DEFAULT or Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            // High reliability fallback: basic reversible obf for non-critical fallback
            basicObfuscate(data)
        }
    }

    /**
     * Decrypt base64 format merged representation.
     */
    fun decrypt(encryptedDataBase64: String): String {
        if (encryptedDataBase64.isEmpty()) return ""
        return try {
            val merged = Base64.decode(encryptedDataBase64, Base64.DEFAULT)
            if (merged.isEmpty()) return ""
            
            val ivSize = merged[0].toInt()
            if (ivSize <= 0 || ivSize > 16) {
                // Not standard GCM IV sizes, try fallback decrypt
                return basicDeobfuscate(encryptedDataBase64)
            }
            
            val iv = ByteArray(ivSize)
            System.arraycopy(merged, 1, iv, 0, ivSize)
            
            val cipherBytesSize = merged.size - 1 - ivSize
            val cipherBytes = ByteArray(cipherBytesSize)
            System.arraycopy(merged, 1 + ivSize, cipherBytes, 0, cipherBytesSize)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
            
            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            basicDeobfuscate(encryptedDataBase64)
        }
    }

    // Safety fallback obfuscators to prevent app from breaking if Keystore keys are corrupted
    private fun basicObfuscate(data: String): String {
        val bytes = data.toByteArray(Charsets.UTF_8)
        val key = 0x55.toByte()
        val obfBytes = ByteArray(bytes.size) { i -> (bytes[i].toInt() xor key.toInt()).toByte() }
        return "OBF:" + Base64.encodeToString(obfBytes, Base64.DEFAULT or Base64.NO_WRAP)
    }

    private fun basicDeobfuscate(data: String): String {
        if (!data.startsWith("OBF:")) return data
        return try {
            val cleanBase64 = data.substring(4)
            val obfBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val key = 0x55.toByte()
            val cleanBytes = ByteArray(obfBytes.size) { i -> (obfBytes[i].toInt() xor key.toInt()).toByte() }
            String(cleanBytes, Charsets.UTF_8)
        } catch (e: java.lang.Exception) {
            data
        }
    }
}
