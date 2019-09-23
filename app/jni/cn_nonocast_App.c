#include <stdio.h>
#include "cn_nonocast_App.h"

int add(int, int);

// libcalcJNI.jnilib
JNIEXPORT jint JNICALL Java_cn_nonocast_App_calc(JNIEnv* env, jclass c, jint a, jint b) {
  return add(a, b);
}
