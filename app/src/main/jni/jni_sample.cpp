#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <algorithm>
#include "FFT4g.h"
#include <vector>

#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/contrib/detection_based_tracker.hpp>

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT jstring JNICALL Java_com_taku_kobayashi_jnisample_MainActivity_hello(JNIEnv *env,
                                                                               jobject thiz) {
    //__android_log_print(ANDROID_LOG_INFO, __FILE__, "hoge");
    //when use C
    //return (*env)->NewStringUTF(env, "Hello world!");
    return env->NewStringUTF("Hello world!");
}

JNIEXPORT jintArray JNICALL Java_sing_narcis_com_narcissing_JniSampleActivity_convert(JNIEnv *env,
                                                                                      jobject obj,
                                                                                      jintArray src,
                                                                                      jint width,
                                                                                      jint height) {
    jint *arr = env->GetIntArrayElements(src, 0);
    int totalPixel = width * height;
    jintArray r = env->NewIntArray(totalPixel);
    jint *narr = env->GetIntArrayElements(r, 0);
    for (int i = 0; i < totalPixel; i++) {
        int alpha = (arr[i] & 0xFF000000) >> 24;
        int red = (arr[i] & 0x00FF0000) >> 16;
        int green = (arr[i] & 0x0000FF00) >> 8;
        int blue = (arr[i] & 0x000000FF);
        //ここに計算処理を色々と書く。
        narr[i] = (alpha << 24) | (blue << 16) | (red << 8) | green;
    }
    env->ReleaseIntArrayElements(src, arr, 0);
    env->ReleaseIntArrayElements(r, narr, 0);
    return r;
}

JNIEXPORT jintArray JNICALL Java_sing_narcis_com_narcissing_JniSampleActivity_grayscale(JNIEnv *env,
                                                                                        jobject obj,
                                                                                        jintArray src,
                                                                                        jint width,
                                                                                        jint height,
                                                                                        jint value) {
    jint *arr = env->GetIntArrayElements(src, 0);
    int totalPixel = width * height;
    jintArray r = env->NewIntArray(totalPixel);
    jint *narr = env->GetIntArrayElements(r, 0);
    for (int i = 0; i < totalPixel; i++) {
        int alpha = (arr[i] & 0xFF000000) >> 24;
        int red = (arr[i] & 0x00FF0000) >> 16;
        int green = (arr[i] & 0x0000FF00) >> 8;
        int blue = (arr[i] & 0x000000FF);
        //ここに計算処理を色々と書く。
        int gray = (int) (0.298912 * blue + 0.586611 * green + 0.114478 * red);
        int nSep = gray / value;
        int v;
        if (value != 1 && 128 <= nSep * value) {
            v = std::min((nSep + 1) * value, 256);
        } else {
            v = nSep * value;
        }
        narr[i] = (alpha << 24) | (v << 16) | (v << 8) | v;
    }
    env->ReleaseIntArrayElements(src, arr, 0);
    env->ReleaseIntArrayElements(r, narr, 0);
    return r;
}

JNIEXPORT jintArray JNICALL Java_sing_narcis_com_narcissing_JniSampleActivity_decodeYUV420SP(
        JNIEnv *env, jobject obj, jbyteArray yuv420sp, jint width, jint height) {
    jbyte *yuv420 = env->GetByteArrayElements(yuv420sp, 0);
    int frameSize = width * height;
    jintArray r = env->NewIntArray(frameSize);
    jint *narr = env->GetIntArrayElements(r, 0);
    for (int j = 0, yp = 0; j < height; j++) {
        int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
        for (int i = 0; i < width; ++i, ++yp) {
            int y = (0xff & ((int) yuv420[yp])) - 16;
            if (y < 0) y = 0;
            if ((i & 1) == 0) {
                v = (0xff & yuv420[uvp++]) - 128;
                u = (0xff & yuv420[uvp++]) - 128;
            }

            int y1192 = 1192 * y;
            int r = (y1192 + 1634 * v);
            int g = (y1192 - 833 * v - 400 * u);
            int b = (y1192 + 2066 * u);

            if (r < 0) r = 0; else if (r > 262143) r = 262143;
            if (g < 0) g = 0; else if (g > 262143) g = 262143;
            if (b < 0) b = 0; else if (b > 262143) b = 262143;

            narr[yp] =
                    0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        }
    }
    env->ReleaseByteArrayElements(yuv420sp, yuv420, 0);
    env->ReleaseIntArrayElements(r, narr, 0);
    return r;
}
JNIEXPORT void JNICALL Java_sing_narcis_com_narcissing_AudioRecordThread_FFTrdft(
        JNIEnv *env, jobject obj, jint size, jint isgn, jdoubleArray fft_data) {
    jdoubleArray fft_doubles = env->NewDoubleArray(size);
    jdouble *darr = env->GetDoubleArrayElements(fft_data, 0);
    FFT4g *fft4g = new FFT4g(sizeof(fft_doubles));
    fft4g->rdft(isgn, darr);
    env->ReleaseDoubleArrayElements(fft_doubles, darr, 0);
}

JNIEXPORT jintArray JNICALL Java_sing_narcis_com_narcissing_JniSampleActivity_mosaic(JNIEnv *env,
                                                                                     jobject obj,
                                                                                     jintArray src,
                                                                                     jint width,
                                                                                     jint height,
                                                                                     jint dot) {
    jint *arr = env->GetIntArrayElements(src, 0);
    int square = dot * dot;
    jintArray r = env->NewIntArray(width * height);
    jint *narr = env->GetIntArrayElements(r, 0);
    for (int w = 0, widthEnd = width / dot; w < widthEnd; w++) {
        for (int h = 0, heightEnd = height / dot; h < heightEnd; h++) {
            // ドットの中の平均値を使う
            int alpha = 0;
            int red = 0;
            int green = 0;
            int blue = 0;
            int moveX = w * dot;
            int moveY = h * dot;
            for (int dw = 0; dw < dot; dw++) {
                for (int dh = 0; dh < dot; dh++) {
                    int dotColor = arr[moveX + dw + (moveY + dh) * width];
                    alpha = alpha + ((dotColor & 0xFF000000) >> 24);
                    red = red + ((dotColor & 0x00FF0000) >> 16);
                    green = green + ((dotColor & 0x0000FF00) >> 8);
                    blue = blue + (dotColor & 0x000000FF);
                }
            }
            alpha = alpha / square;
            red = red / square;
            green = green / square;
            blue = blue / square;
            for (int dw = 0; dw < dot; dw++) {
                for (int dh = 0; dh < dot; dh++) {
                    narr[moveX + dw + (moveY + dh) * width] = (alpha << 24) | (red << 16) | (green << 8) | blue;
                }
            }
        }
    }
    env->ReleaseIntArrayElements(src, arr, 0);
    env->ReleaseIntArrayElements(r, narr, 0);
    return r;
}

JNIEXPORT jintArray JNICALL Java_sing_narcis_com_narcissing_JniSampleActivity_approximateColor(JNIEnv *env,
                                                                                      jobject obj,
                                                                                      jintArray src,
                                                                                      jint width,
                                                                                      jint height,
                                                                                      jint targetColor,
                                                                                      jint threshold) {
    jint *arr = env->GetIntArrayElements(src, 0);
    int totalPixel = width * height;
    jintArray r = env->NewIntArray(totalPixel);
    jint *narr = env->GetIntArrayElements(r, 0);
    int targetAlpha = (targetColor & 0xFF000000) >> 24;
    int targetRed = (targetColor & 0x00FF0000) >> 16;
    int targetGreen = (targetColor & 0x0000FF00) >> 8;
    int targetBlue = (targetColor & 0x000000FF);
    for (int i = 0; i < totalPixel; i++) {
        int alpha = (arr[i] & 0xFF000000) >> 24;
        int red = (arr[i] & 0x00FF0000) >> 16;
        int green = (arr[i] & 0x0000FF00) >> 8;
        int blue = (arr[i] & 0x000000FF);
        int a = alpha - targetAlpha;
        int r = red - targetRed;
        int g = green - targetAlpha;
        int b = blue - targetBlue;
        //__android_log_print(ANDROID_LOG_VERBOSE, "narcissing", "l:%d %i", __LINE__, alpha);
        if(threshold < std::sqrt(r * r + g * g + b * b)){
            //alphaは全て255のだがalphaを0にして送り返してもRGBだけで見るようになってしまって無意味なのでこうする
            narr[i] = 0;
        }else{
            narr[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }
        //__android_log_print(ANDROID_LOG_VERBOSE, "NativeCode", "%s(%d): %i", __FILE__, __LINE__, narr[i]);
    }
    env->ReleaseIntArrayElements(src, arr, 0);
    env->ReleaseIntArrayElements(r, narr, 0);
    return r;
}

JNIEXPORT jintArray JNICALL Java_sing_narcis_com_narcissing_JniSampleActivity_noiseRemove(JNIEnv *env,
                                                                                      jobject obj,
                                                                                      jintArray src,
                                                                                      jint width,
                                                                                      jint height) {
    jint *arr = env->GetIntArrayElements(src, 0);
    int totalPixel = width * height;
    jintArray r = env->NewIntArray(totalPixel);
    jint *narr = env->GetIntArrayElements(r, 0);
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            std::vector<int> data;
            for (int yy = -1; yy <= 1; ++yy) {
                for (int xx = -1; xx <= 1; ++xx) {
                    if (x + xx < 0 || width <= x + xx || y + yy < 0 || height <= y + yy) continue;
                    data.push_back(arr[(y + yy) * width + x + xx]);

                }
            }
            std::sort(data.begin(), data.end());
            narr[y * width + x] = data[(sizeof(data) / 2) + 1];
            data.clear();
        }
    }
    env->ReleaseIntArrayElements(src, arr, 0);
    env->ReleaseIntArrayElements(r, narr, 0);
    return r;
}

JNIEXPORT jintArray JNICALL Java_sing_narcis_com_narcissing_FaceOverlayImageView_facedetect(JNIEnv *env,
                                                                                     jobject obj,
                                                                                     jintArray src,
                                                                                     jint width,
                                                                                     jint height
                                                                                     ) {
    jint *arr = env->GetIntArrayElements(src, 0);
    int totalPixel = width * height;
    jintArray r = env->NewIntArray(totalPixel);
    jint *narr = env->GetIntArrayElements(r, 0);
    //Mat intMat_BGRA = Mat(width,height, narr);
    for (int i = 0; i < totalPixel; i++) {
        int alpha = (arr[i] & 0xFF000000) >> 24;
        int red = (arr[i] & 0x00FF0000) >> 16;
        int green = (arr[i] & 0x0000FF00) >> 8;
        int blue = (arr[i] & 0x000000FF);
        //ここに計算処理を色々と書く。
        narr[i] = (alpha << 24) | (blue << 16) | (red << 8) | green;
    }
    env->ReleaseIntArrayElements(src, arr, 0);
    env->ReleaseIntArrayElements(r, narr, 0);
    return r;
}
}