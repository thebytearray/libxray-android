package org.thebytearray.libxray.sdk

/**
 * Called from native code when a socket FD should be excluded from the VPN tunnel
 * (typically forward to [android.net.VpnService.protect]).
 *
 * Return `true` if the FD was protected successfully.
 */
fun interface ProtectHandler {
    fun onProtectFd(fd: Int): Boolean
}
