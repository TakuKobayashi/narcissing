package sing.narcis.com.narcissing;

public class NativeHelper {
    static {
        System.loadLibrary("jni_sample");
    }

    public static native void FFTrdft(int size, int isgn, double[] fft_data);
    public static native int[] convert(int[] pixcels,int width, int height);
    public static native int[] grayscale(int[] pixcels,int width, int height,int value);
    public static native int[] decodeYUV420SP(byte[] yuv,int width, int height);
    public static native int[] mosaic(int[] pixcels,int width, int height,int dot);
    public static native int[] approximateColor(int[] pixcels,int width, int height,int targetColor, int threshold);
    public static native int[] noiseRemove(int[] pixcels,int width, int height);
    public static native int[] negative(int[] pixcels,int width, int height);
    public static native int[] brightness(int[] pixcels,int width, int height);
    public static native int[] facedetect(int[] pixcels,int width, int height);
    public static native int[] posterize(int[] pixcels,int width, int height,int step);
}
