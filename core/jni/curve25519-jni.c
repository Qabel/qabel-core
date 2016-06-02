#include <jni.h>
#include <stdlib.h>

#include "curve25519-jni.h"
#include "curve25519.h"

JNIEXPORT jbyteArray JNICALL
Java_de_qabel_core_crypto_Curve25519_cryptoScalarmult(JNIEnv * env, jobject obj, jbyteArray n, jbyteArray p)
{
	jboolean qIsCopy, pIsCopy;
	jbyte *_n, *_p;
	jbyteArray result;
	jsize length;

	length = (*env)->GetArrayLength(env, n);
	jbyte _result[length];
	result = (*env)->NewByteArray(env, length);

	_n = (*env)->GetByteArrayElements(env, n, &qIsCopy);
	_p = (*env)->GetByteArrayElements(env, p, &pIsCopy);

	if (_n == NULL || _p == NULL) {
		jclass Exception = (*env)->FindClass(env, "java/lang/RuntimeException");
		(*env)->ThrowNew(env, Exception, "Could not get byte array elements!");
	}

	crypto_scalarmult((unsigned char *) _result, (unsigned char *) _n, (unsigned char *) _p);

	if (qIsCopy) {
		(*env)->ReleaseByteArrayElements(env, n, _n, JNI_ABORT);
	}
	if (pIsCopy) {
		(*env)->ReleaseByteArrayElements(env, p, _p, JNI_ABORT);
	}

	(*env)->SetByteArrayRegion(env, result, 0, length, _result);

	return result;
}

JNIEXPORT jbyteArray JNICALL
Java_de_qabel_core_crypto_Curve25519_cryptoScalarmultBase(JNIEnv * env, jobject obj, jbyteArray n)
{
	jboolean qIsCopy;
	jbyte *_n;
	jbyteArray result;
	jsize length;

	length = (*env)->GetArrayLength(env, n);
	jbyte _result[length];
	result = (*env)->NewByteArray(env, length);

	_n = (*env)->GetByteArrayElements(env, n, &qIsCopy);

	if (_n == NULL) {
		jclass Exception = (*env)->FindClass(env, "java/lang/RuntimeException");
		(*env)->ThrowNew(env, Exception, "Could not get byte array elements!");
	}

	crypto_scalarmult_base((unsigned char *) _result, (unsigned char *) _n);

	if (qIsCopy) {
		(*env)->ReleaseByteArrayElements(env, n, _n, JNI_ABORT);
	}

	(*env)->SetByteArrayRegion(env, result, 0, length, _result);

	return result;
}
