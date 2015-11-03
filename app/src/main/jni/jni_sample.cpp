#include <jni.h>
#include <string.h>
#include <android/log.h>

extern "C" {
JNIEXPORT jstring JNICALL Java_com_taku_kobayashi_jnisample_MainActivity_hello(JNIEnv *env, jobject thiz) {
  //__android_log_print(ANDROID_LOG_INFO, __FILE__, "hoge");
  //when use C
  //return (*env)->NewStringUTF(env, "Hello world!");
  return env->NewStringUTF("Hello world!");
}

JNIEXPORT void JNICALL Java_sing_narcis_com_narcissing_JniSampleActivity_convert(JNIEnv* env , jobject obj, jintArray src , jint width, jint height ) {
   int i, totalPixel;
   jint *arr = env->GetIntArrayElements(src, 0);
   totalPixel = width * height;
   for ( i = 0; i<totalPixel; i ++ ) {
      int alpha = (arr[i] & 0xFF000000) >> 24;
      int red = (arr[i] & 0x00FF0000) >> 16;
      int green = (arr[i] & 0x0000FF00) >> 8;
      int blue = (arr[i] & 0x000000FF);
      //ここに計算処理を色々と書く。
      arr [ i ] = ( alpha << 24 ) | ( blue << 16 ) | ( red << 8 ) | green;
    }
    env->ReleaseIntArrayElements(src, arr,0);
  }
}
