package sing.narcis.com.narcissing;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioRecordThread extends ContextSingletonBase<AudioRecordThread> {
    static {
        System.loadLibrary("jni_sample");
    }

    private static final int SAMPLING_RATE = 44100;
    private byte[] mRecordingBuffer;
    private AudioRecord mAudioRecord = null;
    private boolean bIsRecording;
    private double baseValue;
    private AudioRecordCallback mCallback;
    private static final int FFT_SIZE = 4096;
    // デシベルベースラインの設定
    double dB_baseline = Math.pow(2, 15) * FFT_SIZE * Math.sqrt(2);

    // 分解能の計算
    double resol = ((SAMPLING_RATE / (double) FFT_SIZE));


    private native void FFTrdft(int size, int isgn, double[] fft_data);

    public void init(Context context) {
        super.init(context);
    }

    public void startRecording() {
        baseValue = 12.0;
        mRecordingBuffer = new byte[AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2];
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mRecordingBuffer.length);

        mAudioRecord.startRecording();
        bIsRecording = true;
        // 録音スレッド
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (bIsRecording) {
                    // 録音データ読み込み
                    int read = mAudioRecord.read(mRecordingBuffer, 0, mRecordingBuffer.length);
                    if (read < 0) {
                        throw new IllegalStateException();
                    }
                    ByteBuffer bf = ByteBuffer.wrap(mRecordingBuffer);
                    bf.order(ByteOrder.LITTLE_ENDIAN);
                    short[] s = new short[(int) mRecordingBuffer.length];
                    for (int i = bf.position(); i < bf.capacity() / 2; i++) {
                        s[i] = bf.getShort();
                    }
                    //Log.d(Config.DEBUG_KEY, "buffered:" + mRecordingBuffer.length);

                    double[] FFTdata = new double[FFT_SIZE];
                    for (int i = 0; i < FFT_SIZE; i++) {
                        FFTdata[i] = (double) s[i];
                    }
                    FFTrdft(FFT_SIZE, 1, FFTdata);
                    // デシベルの計算
                    double[] dbfs = new double[FFT_SIZE / 2];
                    double max_db = -120d;
                    int max_i = 0;
                    for (int i = 0; i < FFT_SIZE; i += 2) {
                        dbfs[i / 2] = (int) (20 * Math.log10(Math.sqrt(Math.pow(FFTdata[i], 2)
                                + Math.pow(FFTdata[i + 1], 2)) / dB_baseline));
                        if (max_db < dbfs[i / 2]) {
                            max_db = dbfs[i / 2];
                            max_i = i / 2;
                        }
                    }
                    //音量が最大の周波数と，その音量を表示
                    Log.d("fft","周波数："+ resol * max_i+" [Hz] 音量：" +  max_db+" [dB]");

                    if(mCallback != null){
                        mCallback.onRecord(mRecordingBuffer, max_db);
                    }
                }
                mAudioRecord.stop();
            }
        }).start();
    }

    public void stopRecording() {
        bIsRecording = false;
    }

    public void release() {
        mCallback = null;
        stopRecording();
    }

    public void setOnAudioRecordCallback(AudioRecordCallback callback) {
        mCallback = callback;
    }

    public void removeOnAudioRecordCallback() {
        mCallback = null;
    }

    public interface AudioRecordCallback{
        public void onRecord(byte[] raw, double decibel);
    }

    //デストラクタ
    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }
}
