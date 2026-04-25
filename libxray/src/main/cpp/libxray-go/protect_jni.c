#include "protect_jni.h"
#include <stdlib.h>
#include <string.h>
#include <pthread.h>

static JavaVM* g_vm;
static pthread_mutex_t g_lock = PTHREAD_MUTEX_INITIALIZER;
static jobject g_handler;
static jmethodID g_on_protect_fd;

/*
 * Registers `handler.onProtectFd(int): boolean`, or clears the previous handler when null.
 * Thread-safe with libxray_jni_call_protect. Returns 0 on success, JNI error codes, or -1/-2/-3 on setup failure.
 */
jint libxray_jni_register_protect(JNIEnv* env, jobject handler)
{
	JavaVM* vm;
	jint err = (*env)->GetJavaVM(env, &vm);
	if (err != 0) {
		return (jint)err;
	}
	pthread_mutex_lock(&g_lock);
	if (g_handler != NULL) {
		(*env)->DeleteGlobalRef(env, g_handler);
		g_handler = NULL;
	}
	g_on_protect_fd = NULL;
	if (handler == NULL) {
		pthread_mutex_unlock(&g_lock);
		return 0;
	}
	g_vm = vm;
	jclass cls = (*env)->GetObjectClass(env, handler);
	if (!cls) {
		pthread_mutex_unlock(&g_lock);
		return -1;
	}
	g_on_protect_fd = (*env)->GetMethodID(env, cls, "onProtectFd", "(I)Z");
	(*env)->DeleteLocalRef(env, cls);
	if (g_on_protect_fd == NULL) {
		(*env)->ExceptionClear(env);
		pthread_mutex_unlock(&g_lock);
		return -2;
	}
	g_handler = (*env)->NewGlobalRef(env, handler);
	if (g_handler == NULL) {
		pthread_mutex_unlock(&g_lock);
		return -3;
	}
	pthread_mutex_unlock(&g_lock);
	return 0;
}

/* Attaches to the JVM if needed, calls the handler, returns 1 if protection succeeded. */
int libxray_jni_call_protect(int fd)
{
	JNIEnv* env;
	int attached = 0;
	int out = 0;
	jint get_env;

	if (g_vm == NULL) {
		return 0;
	}
	pthread_mutex_lock(&g_lock);
	if (g_handler == NULL || g_on_protect_fd == NULL) {
		pthread_mutex_unlock(&g_lock);
		return 0;
	}
	jobject h = g_handler;
	jmethodID mid = g_on_protect_fd;
	pthread_mutex_unlock(&g_lock);

	{
		void* ep = NULL;
		get_env = (*g_vm)->GetEnv(g_vm, &ep, JNI_VERSION_1_6);
		env = (JNIEnv*)ep;
	}
	if (get_env == JNI_EDETACHED) {
		JNIEnv* ap_env = NULL;
		if ((*g_vm)->AttachCurrentThread(g_vm, &ap_env, NULL) != 0) {
			return 0;
		}
		env = ap_env;
		attached = 1;
	} else if (get_env != JNI_OK) {
		return 0;
	}

	{
		jboolean ok = (*env)->CallBooleanMethod(env, h, mid, (jint)fd);
		if ((*env)->ExceptionCheck(env)) {
			(*env)->ExceptionClear(env);
			out = 0;
		} else {
			out = (ok == JNI_TRUE) ? 1 : 0;
		}
	}

	if (attached) {
		(*g_vm)->DetachCurrentThread(g_vm);
	}
	return out;
}
