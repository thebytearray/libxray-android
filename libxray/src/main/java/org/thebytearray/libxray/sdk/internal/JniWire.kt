package org.thebytearray.libxray.sdk.internal

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * Base64 UTF-8 encoding used between Kotlin and the native JSON payloads (JNI passes strings as a single “wire” form).
 */
internal object JniWire {
    private val utf8 = StandardCharsets.UTF_8
    private const val FLAGS = Base64.NO_WRAP

    fun toWire(plain: String): String = Base64.encodeToString(plain.toByteArray(utf8), FLAGS)

    fun toPlain(wire: String): String {
        if (wire.isEmpty()) return wire
        return runCatching {
            val bytes = Base64.decode(wire, FLAGS)
            String(bytes, utf8)
        }.getOrElse { wire }
    }

    /**
     * Normalizes native responses from [org.thebytearray.libxray.sdk.LibXrayJni.newXrayRunRequest] and
     * [org.thebytearray.libxray.sdk.LibXrayJni.newXrayRunFromJSONRequest]: unwraps `{"d":"..."}` payloads
     * and maps `{"e":...}` into a plain `{"success":false,"error":"..."}` JSON string.
     */
    fun unwrapNewRequestPayload(jsonFromNative: String): String {
        val t = jsonFromNative.trim()
        if (!t.startsWith("{")) return toPlain(t)
        return runCatching {
            val o = JSONObject(t)
            if (o.has("d")) {
                return toPlain(o.getString("d"))
            }
            if (o.has("e")) {
                val err = o.opt("e")
                val msg = when (err) {
                    is String -> err
                    else -> err?.toString() ?: ""
                }
                if (msg.isNotEmpty()) {
                    return """{"success":false,"error":${JSONObject.quote(msg)}}"""
                }
            }
            t
        }.getOrElse { toPlain(t) }
    }
}
