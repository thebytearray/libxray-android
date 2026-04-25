package org.thebytearray.libxray.sdk

import org.thebytearray.libxray.sdk.internal.JniWire

/**
 * Primary API for the bundled Xray core on Android.
 *
 * Methods that take `plainRequestJson` use normal UTF-8 JSON on the Kotlin side; values are Base64-wrapped only
 * across JNI (see the internal wire codec in this module).
 */
object LibXray {

    /** Gives the core the TUN device file descriptor (typically from [android.net.VpnService]). */
    @JvmStatic
    fun setTunFd(fd: Int) = LibXrayJni.setTunFd(fd)

    /** Hooks dial/listen into Android so sockets can be protected; call before starting Xray. */
    @JvmStatic
    fun registerAndroidCallbacks() = LibXrayJni.registerAndroidCallbacks()

    /**
     * Installs the callback used to exclude sockets from the VPN (typically [android.net.VpnService.protect]).
     * Returns `0` on success; non-zero on failure (e.g. missing [ProtectHandler.onProtectFd]).
     */
    @JvmStatic
    fun registerProtectHandler(handler: ProtectHandler?): Int = LibXrayJni.registerProtectHandler(handler)

    /** Runs a geo-data count or summary; input and output are JSON strings. */
    @JvmStatic
    fun countGeoData(plainRequestJson: String): String =
        JniWire.toPlain(LibXrayJni.countGeoData(JniWire.toWire(plainRequestJson))!!)

    /** Loads or refreshes geo IP / site files according to the request JSON. */
    @JvmStatic
    fun readGeoFiles(plainRequestJson: String): String =
        JniWire.toPlain(LibXrayJni.readGeoFiles(JniWire.toWire(plainRequestJson))!!)

    /** Measures reachability or latency to an outbound; request and reply are JSON. */
    @JvmStatic
    fun ping(plainRequestJson: String): String =
        JniWire.toPlain(LibXrayJni.ping(JniWire.toWire(plainRequestJson))!!)

    /** Returns traffic or internal stats as JSON for the given stats request. */
    @JvmStatic
    fun queryStats(plainRequestJson: String): String =
        JniWire.toPlain(LibXrayJni.queryStats(JniWire.toWire(plainRequestJson))!!)

    /** Validates or smoke-tests configuration without keeping Xray running; result is JSON. */
    @JvmStatic
    fun testXray(plainRequestJson: String): String =
        JniWire.toPlain(LibXrayJni.testXray(JniWire.toWire(plainRequestJson))!!)

    /** Returns the Xray core version as a plain string (still passed through the wire decoder). */
    @JvmStatic
    fun xrayVersion(): String = JniWire.toPlain(LibXrayJni.xrayVersion()!!)

    /** Starts Xray using a run request that points at config files on disk; result is JSON. */
    @JvmStatic
    fun runXray(plainRequestJson: String): String =
        JniWire.toPlain(LibXrayJni.runXray(JniWire.toWire(plainRequestJson))!!)

    /** Starts Xray using inline config JSON inside the run request; result is JSON. */
    @JvmStatic
    fun runXrayFromJSON(plainRequestJson: String): String =
        JniWire.toPlain(LibXrayJni.runXrayFromJSON(JniWire.toWire(plainRequestJson))!!)

    /** `true` if the core reports Xray is currently running. */
    @JvmStatic
    fun getXrayState(): Boolean = LibXrayJni.getXrayState()

    /** Stops Xray; returns a plain JSON status string. */
    @JvmStatic
    fun stopXray(): String = JniWire.toPlain(LibXrayJni.stopXray()!!)

    /**
     * Builds a **plain** JSON run request from paths (`datDir`, `mphCachePath`, `configPath`) for [runXray].
     * On failure, returns a small JSON error object from the native run-request response.
     */
    @JvmStatic
    fun newXrayRunRequest(datDir: String, mphCachePath: String, configPath: String): String =
        JniWire.unwrapNewRequestPayload(
            LibXrayJni.newXrayRunRequest(datDir, mphCachePath, configPath)!!,
        )

    /**
     * Like [newXrayRunRequest], but the config is passed as a JSON string instead of a file path,
     * for use with [runXrayFromJSON].
     */
    @JvmStatic
    fun newXrayRunFromJSONRequest(datDir: String, mphCachePath: String, configJSON: String): String =
        JniWire.unwrapNewRequestPayload(
            LibXrayJni.newXrayRunFromJSONRequest(datDir, mphCachePath, configJSON)!!,
        )

    /** Builds or updates the MPH cache file described by the request JSON. */
    @JvmStatic
    fun buildMphCache(plainRequestJson: String): String =
        JniWire.toPlain(LibXrayJni.buildMphCache(JniWire.toWire(plainRequestJson))!!)

    /** Asks native code for [count] free local TCP ports; reply is JSON. */
    @JvmStatic
    fun getFreePorts(count: Int): String = JniWire.toPlain(LibXrayJni.getFreePorts(count)!!)

    /** Converts share / subscription links into an Xray JSON config (per request JSON). */
    @JvmStatic
    fun convertShareLinksToXrayJson(plainRequestJson: String): String =
        JniWire.toPlain(LibXrayJni.convertShareLinksToXrayJson(JniWire.toWire(plainRequestJson))!!)

    /** Converts an Xray JSON config into share links (per request JSON). */
    @JvmStatic
    fun convertXrayJsonToShareLinks(plainRequestJson: String): String =
        JniWire.toPlain(LibXrayJni.convertXrayJsonToShareLinks(JniWire.toWire(plainRequestJson))!!)

    /** Sets a custom DNS server address for the core; empty or default behavior depends on the native layer. */
    @JvmStatic
    fun initDns(dnsServer: String) = LibXrayJni.initDnsServer(dnsServer)

    /** Clears custom DNS set by [initDns]. */
    @JvmStatic
    fun resetDns() = LibXrayJni.resetDns()
}
