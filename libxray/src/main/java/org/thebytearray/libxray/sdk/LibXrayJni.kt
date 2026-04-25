package org.thebytearray.libxray.sdk

/**
 * JNI bindings to `libxray-go`. Class and package names must match the `JNIEXPORT` symbols in `jni.c`.
 *
 * [base64Text] arguments are UTF-8 JSON, Base64-encoded for the JNI wire format.
 */
internal object LibXrayJni {
    init {
        System.loadLibrary("xray-go")
    }

    @JvmStatic
    external fun setTunFd(fd: Int)

    @JvmStatic
    external fun countGeoData(base64Text: String): String?

    @JvmStatic
    external fun readGeoFiles(base64Text: String): String?

    @JvmStatic
    external fun ping(base64Text: String): String?

    @JvmStatic
    external fun queryStats(base64Text: String): String?

    @JvmStatic
    external fun testXray(base64Text: String): String?

    @JvmStatic
    external fun runXray(base64Text: String): String?

    @JvmStatic
    external fun runXrayFromJSON(base64Text: String): String?

    @JvmStatic
    external fun getXrayState(): Boolean

    @JvmStatic
    external fun stopXray(): String?

    @JvmStatic
    external fun xrayVersion(): String?

    @JvmStatic
    external fun buildMphCache(base64Text: String): String?

    @JvmStatic
    external fun getFreePorts(count: Int): String?

    @JvmStatic
    external fun convertShareLinksToXrayJson(base64Text: String): String?

    @JvmStatic
    external fun convertXrayJsonToShareLinks(base64Text: String): String?

    @JvmStatic
    external fun newXrayRunRequest(datDir: String, mphCachePath: String, configPath: String): String?

    @JvmStatic
    external fun newXrayRunFromJSONRequest(datDir: String, mphCachePath: String, configJSON: String): String?

    @JvmStatic
    external fun registerAndroidCallbacks()

    @JvmStatic
    external fun initDnsServer(server: String?)

    @JvmStatic
    external fun resetDns()

    @JvmStatic
    external fun registerProtectHandler(handler: ProtectHandler?): Int
}
