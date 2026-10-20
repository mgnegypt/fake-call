package com.mgneg.fakecall.update

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

object Sha256 {
    fun hex(input: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun hex(file: File): String = file.inputStream().use { hex(it) }

    /** The .sha256 asset format is "<hex>  <filename>" (or just the hex). */
    fun expectedFromFileContent(content: String): String? =
        content.trim().split(Regex("\\s+")).firstOrNull()
            ?.takeIf { it.matches(Regex("[0-9a-fA-F]{64}")) }
            ?.lowercase()

    fun verify(file: File, expectedHex: String): Boolean =
        runCatching { hex(file).equals(expectedHex, ignoreCase = true) }.getOrDefault(false)
}
