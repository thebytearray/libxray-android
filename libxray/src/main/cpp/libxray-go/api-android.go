//go:build android
// +build android

package main

/*
#cgo LDFLAGS: -llog
#include <stdlib.h>
int libxray_jni_call_protect(int fd);
*/
import "C"
import (
	"encoding/json"

	libXray "github.com/xtls/libxray"
)

// jniProtectController forwards socket protection to Java (VpnService.protect) via JNI.
type jniProtectController struct{}

func (jniProtectController) ProtectFd(fd int) bool {
	return C.libxray_jni_call_protect(C.int(fd)) != 0
}

func goStr(p *C.char) string {
	if p == nil {
		return ""
	}
	return C.GoString(p)
}

func cStr(s string) *C.char { return C.CString(s) }

// cStrErr returns a small JSON object {"e":...} for JNI to turn into a Kotlin error payload.
func cStrErr(e error) *C.char {
	if e == nil {
		return cStr(`{"e":""}`)
	}
	b, _ := json.Marshal(e.Error())
	return cStr(`{"e":` + string(b) + `}`)
}

// cStrData returns {"key": "<escaped val>"} for successful new-run-request payloads.
func cStrData(key, val string) *C.char {
	enc, _ := json.Marshal(val)
	return cStr(`{"` + key + `":` + string(enc) + `}`)
}

// libxraySetTunFd sets the VPN TUN fd in the Xray core.
//
//export libxraySetTunFd
func libxraySetTunFd(fd C.int) { libXray.SetTunFd(int32(fd)) }

// libxrayCountGeoData runs geo data counting; base64Text is the wire request JSON.
//
//export libxrayCountGeoData
func libxrayCountGeoData(base64Text *C.char) *C.char { return cStr(libXray.CountGeoData(goStr(base64Text))) }

// libxrayReadGeoFiles loads geo files per the wire request JSON.
//
//export libxrayReadGeoFiles
func libxrayReadGeoFiles(base64Text *C.char) *C.char { return cStr(libXray.ReadGeoFiles(goStr(base64Text))) }

// libxrayPing probes outbound connectivity; request/response are JSON strings on the wire.
//
//export libxrayPing
func libxrayPing(base64Text *C.char) *C.char { return cStr(libXray.Ping(goStr(base64Text))) }

// libxrayQueryStats returns stats JSON for the given wire request.
//
//export libxrayQueryStats
func libxrayQueryStats(base64Text *C.char) *C.char { return cStr(libXray.QueryStats(goStr(base64Text))) }

// libxrayTestXray validates config / dry run; returns JSON.
//
//export libxrayTestXray
func libxrayTestXray(base64Text *C.char) *C.char { return cStr(libXray.TestXray(goStr(base64Text))) }

// libxrayRunXray starts Xray from a run request that references config files.
//
//export libxrayRunXray
func libxrayRunXray(base64Text *C.char) *C.char { return cStr(libXray.RunXray(goStr(base64Text))) }

// libxrayRunXrayFromJSON starts Xray using inline config inside the run request.
//
//export libxrayRunXrayFromJSON
func libxrayRunXrayFromJSON(base64Text *C.char) *C.char { return cStr(libXray.RunXrayFromJSON(goStr(base64Text))) }

// libxrayGetXrayState returns 1 if the core considers Xray running.
//
//export libxrayGetXrayState
func libxrayGetXrayState() C.int {
	if libXray.GetXrayState() {
		return 1
	}
	return 0
}

// libxrayStopXray stops the core and returns JSON status.
//
//export libxrayStopXray
func libxrayStopXray() *C.char { return cStr(libXray.StopXray()) }

// libxrayXrayVersion returns the Xray version string.
//
//export libxrayXrayVersion
func libxrayXrayVersion() *C.char { return cStr(libXray.XrayVersion()) }

// libxrayBuildMphCache builds the MPH cache described by the wire request JSON.
//
//export libxrayBuildMphCache
func libxrayBuildMphCache(base64Text *C.char) *C.char { return cStr(libXray.BuildMphCache(goStr(base64Text))) }

// libxrayGetFreePorts returns JSON with up to count free ports (count clamped in Go).
//
//export libxrayGetFreePorts
func libxrayGetFreePorts(count C.int) *C.char {
	n := int(count)
	if n < 0 || n > 1024*1024 {
		return cStr(`{"e":"invalid count"}`)
	}
	return cStr(libXray.GetFreePorts(n))
}

// libxrayConvertShareLinksToXrayJson converts subscription/share links to Xray JSON (wire request).
//
//export libxrayConvertShareLinksToXrayJson
func libxrayConvertShareLinksToXrayJson(base64Text *C.char) *C.char {
	return cStr(libXray.ConvertShareLinksToXrayJson(goStr(base64Text)))
}

// libxrayConvertXrayJsonToShareLinks converts Xray JSON back to share links (wire request).
//
//export libxrayConvertXrayJsonToShareLinks
func libxrayConvertXrayJsonToShareLinks(base64Text *C.char) *C.char {
	return cStr(libXray.ConvertXrayJsonToShareLinks(goStr(base64Text)))
}

// libxrayNewXrayRunRequest builds run JSON from filesystem paths; C caller frees the returned string.
//
//export libxrayNewXrayRunRequest
func libxrayNewXrayRunRequest(datDir, mphCachePath, configPath *C.char) *C.char {
	s, e := libXray.NewXrayRunRequest(goStr(datDir), goStr(mphCachePath), goStr(configPath))
	if e != nil {
		return cStrErr(e)
	}
	return cStrData("d", s)
}

// libxrayNewXrayRunFromJSONRequest is like libxrayNewXrayRunRequest but config is inline JSON text.
//
//export libxrayNewXrayRunFromJSONRequest
func libxrayNewXrayRunFromJSONRequest(datDir, mphCachePath, configJSON *C.char) *C.char {
	s, e := libXray.NewXrayRunFromJSONRequest(goStr(datDir), goStr(mphCachePath), goStr(configJSON))
	if e != nil {
		return cStrErr(e)
	}
	return cStrData("d", s)
}

// libxrayRegisterAndroidCallbacks wires dial and listener controllers to JNI protect.
//
//export libxrayRegisterAndroidCallbacks
func libxrayRegisterAndroidCallbacks() {
	c := jniProtectController{}
	libXray.RegisterDialerController(c)
	libXray.RegisterListenerController(c)
}

// libxrayInitDns sets a custom DNS server for the core (empty string allowed).
//
//export libxrayInitDns
func libxrayInitDns(server *C.char) {
	libXray.InitDns(jniProtectController{}, goStr(server))
}

// libxrayResetDns clears custom DNS in the core.
//
//export libxrayResetDns
func libxrayResetDns() { libXray.ResetDns() }

func main() {}
