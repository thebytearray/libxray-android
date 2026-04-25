#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "protect_jni.h"

void libxraySetTunFd(int fd);
char* libxrayCountGeoData(char* base64Text);
char* libxrayReadGeoFiles(char* base64Text);
char* libxrayPing(char* base64Text);
char* libxrayQueryStats(char* base64Text);
char* libxrayTestXray(char* base64Text);
char* libxrayRunXray(char* base64Text);
char* libxrayRunXrayFromJSON(char* base64Text);
int libxrayGetXrayState(void);
char* libxrayStopXray(void);
char* libxrayXrayVersion(void);
char* libxrayBuildMphCache(char* base64Text);
char* libxrayGetFreePorts(int count);
char* libxrayConvertShareLinksToXrayJson(char* base64Text);
char* libxrayConvertXrayJsonToShareLinks(char* base64Text);
char* libxrayNewXrayRunRequest(char* datDir, char* mphCachePath, char* configPath);
char* libxrayNewXrayRunFromJSONRequest(char* datDir, char* mphCachePath, char* configJSON);
void libxrayRegisterAndroidCallbacks(void);
void libxrayInitDns(char* server);
void libxrayResetDns(void);

/* Turns a freshly allocated UTF-8 C string from libxray-go into a jstring and frees the buffer. */
static jstring jstr_from_cstr_free(JNIEnv* env, char* s)
{
	jstring out;
	if (!s) {
		return NULL;
	}
	out = (*env)->NewStringUTF(env, s);
	free(s);
	return out;
}

/* Passes the VPN TUN file descriptor into the Go/Xray core. */
JNIEXPORT void JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_setTunFd(JNIEnv* env, jclass c, jint fd)
{
	(void)env;
	(void)c;
	libxraySetTunFd((int)fd);
}

/* jstring (Base64 JSON) in, JSON string out; maps to libxray* in api-android.go. */
#define JNIS1(name, cname) \
JNIEXPORT jstring JNICALL \
Java_org_thebytearray_libxray_sdk_LibXrayJni_ ## name(JNIEnv* env, jclass c, jstring jarg) \
{ \
	const char* a; \
	char* r; \
	(void)c; \
	if (!jarg) { return NULL; } \
	a = (*env)->GetStringUTFChars(env, jarg, NULL); \
	if (!a) { return NULL; } \
	r = cname((char*)a); \
	(*env)->ReleaseStringUTFChars(env, jarg, a); \
	return jstr_from_cstr_free(env, r); \
}

JNIS1(countGeoData, libxrayCountGeoData)
JNIS1(readGeoFiles, libxrayReadGeoFiles)
JNIS1(ping, libxrayPing)
JNIS1(queryStats, libxrayQueryStats)
JNIS1(testXray, libxrayTestXray)
JNIS1(runXray, libxrayRunXray)
JNIS1(runXrayFromJSON, libxrayRunXrayFromJSON)
JNIS1(buildMphCache, libxrayBuildMphCache)
JNIS1(convertShareLinksToXrayJson, libxrayConvertShareLinksToXrayJson)
JNIS1(convertXrayJsonToShareLinks, libxrayConvertXrayJsonToShareLinks)

#undef JNIS1

/* 1 if Xray is running inside the core, 0 otherwise. */
JNIEXPORT jboolean JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_getXrayState(JNIEnv* env, jclass c)
{
	(void)env;
	(void)c;
	return libxrayGetXrayState() ? JNI_TRUE : JNI_FALSE;
}

/* Stops Xray; returned string is JSON status text. */
JNIEXPORT jstring JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_stopXray(JNIEnv* env, jclass c)
{
	(void)c;
	return jstr_from_cstr_free(env, libxrayStopXray());
}

/* Human-readable Xray version string from the core. */
JNIEXPORT jstring JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_xrayVersion(JNIEnv* env, jclass c)
{
	(void)c;
	return jstr_from_cstr_free(env, libxrayXrayVersion());
}

/* Returns JSON listing up to `count` free local ports (validated in Go). */
JNIEXPORT jstring JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_getFreePorts(JNIEnv* env, jclass c, jint count)
{
	char* r;
	(void)env;
	(void)c;
	r = libxrayGetFreePorts((int)count);
	return jstr_from_cstr_free(env, r);
}

/* Builds a JSON run payload from on-disk paths; result may be {"d":"..."} or {"e":...}. */
JNIEXPORT jstring JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_newXrayRunRequest(JNIEnv* env, jclass c,
		jstring jdat, jstring jmph, jstring jpath)
{
	const char *d, *m, *p;
	char* r;
	(void)c;
	if (!jdat || !jmph || !jpath) {
		return NULL;
	}
	d = (*env)->GetStringUTFChars(env, jdat, NULL);
	m = (*env)->GetStringUTFChars(env, jmph, NULL);
	p = (*env)->GetStringUTFChars(env, jpath, NULL);
	if (!d || !m || !p) {
		if (d) {
			(*env)->ReleaseStringUTFChars(env, jdat, d);
		}
		if (m) {
			(*env)->ReleaseStringUTFChars(env, jmph, m);
		}
		if (p) {
			(*env)->ReleaseStringUTFChars(env, jpath, p);
		}
		return NULL;
	}
	r = libxrayNewXrayRunRequest((char*)d, (char*)m, (char*)p);
	(*env)->ReleaseStringUTFChars(env, jdat, d);
	(*env)->ReleaseStringUTFChars(env, jmph, m);
	(*env)->ReleaseStringUTFChars(env, jpath, p);
	return jstr_from_cstr_free(env, r);
}

/* Same as newXrayRunRequest but config is inline JSON text. */
JNIEXPORT jstring JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_newXrayRunFromJSONRequest(JNIEnv* env, jclass c,
		jstring jdat, jstring jmph, jstring jjson)
{
	const char *d, *m, *j;
	char* r;
	(void)c;
	if (!jdat || !jmph || !jjson) {
		return NULL;
	}
	d = (*env)->GetStringUTFChars(env, jdat, NULL);
	m = (*env)->GetStringUTFChars(env, jmph, NULL);
	j = (*env)->GetStringUTFChars(env, jjson, NULL);
	if (!d || !m || !j) {
		if (d) {
			(*env)->ReleaseStringUTFChars(env, jdat, d);
		}
		if (m) {
			(*env)->ReleaseStringUTFChars(env, jmph, m);
		}
		if (j) {
			(*env)->ReleaseStringUTFChars(env, jjson, j);
		}
		return NULL;
	}
	r = libxrayNewXrayRunFromJSONRequest((char*)d, (char*)m, (char*)j);
	(*env)->ReleaseStringUTFChars(env, jdat, d);
	(*env)->ReleaseStringUTFChars(env, jmph, m);
	(*env)->ReleaseStringUTFChars(env, jjson, j);
	return jstr_from_cstr_free(env, r);
}

/* Registers Android dial/listen controllers (socket protect path) with libxray. */
JNIEXPORT void JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_registerAndroidCallbacks(JNIEnv* env, jclass c)
{
	(void)env;
	(void)c;
	libxrayRegisterAndroidCallbacks();
}

/* Optional custom resolver address; null clears to default handling in Go. */
JNIEXPORT void JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_initDnsServer(JNIEnv* env, jclass c, jstring jserver)
{
	const char* s;
	(void)c;
	if (!jserver) {
		libxrayInitDns(NULL);
		return;
	}
	s = (*env)->GetStringUTFChars(env, jserver, NULL);
	if (!s) {
		return;
	}
	libxrayInitDns((char*)s);
	(*env)->ReleaseStringUTFChars(env, jserver, s);
}

/* Resets DNS customization in the core. */
JNIEXPORT void JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_resetDns(JNIEnv* env, jclass c)
{
	(void)env;
	(void)c;
	libxrayResetDns();
}

/* Stores a global ref to ProtectHandler; returns 0 on success. See protect_jni.c. */
JNIEXPORT jint JNICALL
Java_org_thebytearray_libxray_sdk_LibXrayJni_registerProtectHandler(JNIEnv* env, jclass c, jobject handler)
{
	(void)c;
	return libxray_jni_register_protect(env, handler);
}
