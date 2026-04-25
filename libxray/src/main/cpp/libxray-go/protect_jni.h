#ifndef PROTECT_JNI_H
#define PROTECT_JNI_H

#include <jni.h>

/* Save or clear the Java ProtectHandler; return 0 on success, negative on JNI errors. */
jint libxray_jni_register_protect(JNIEnv* env, jobject handler);

/* Invoke onProtectFd on the handler from a native thread; returns 1 if Java returned true. */
int libxray_jni_call_protect(int fd);

#endif
