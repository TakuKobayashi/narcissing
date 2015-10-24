package sing.narcis.com.narcissing;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioRecordThread extends ContextSingletonBase<AudioRecordThread> {
    private static final int SAMPLING_RATE = 44100;
    // FFTのポイント数
    private static final int FFT_SIZE = 4096;
    private byte[] mRecordingBuffer;
    private AudioRecord mAudioRecord = null;
    private boolean bIsRecording;
    private double baseValue;
    private AudioRecordCallback mCallback;

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
                    Log.d(Config.DEBUG_KEY, "buffered:" + mRecordingBuffer.length);

                    int maxValue = 0;
                    for (int i = 0; i < read; i++) {
                        maxValue = Math.max(maxValue, mRecordingBuffer[0]);
                    }

                    //エンディアン変換
                    ByteBuffer bf = ByteBuffer.wrap(mRecordingBuffer);
                    bf.order(ByteOrder.LITTLE_ENDIAN);
                    short[] s = new short[mRecordingBuffer.length];
                    for (int i = bf.position(); i < bf.capacity() / 2; i++) {
                        s[i] = bf.getShort();
                    }

                    //FFTクラスの作成と値の引き渡し
                    FFT4g fft = new FFT4g(FFT_SIZE);
                    double[] FFTdata = new double[FFT_SIZE];
                    for (int i = 0; i < FFT_SIZE; i++) {
                        FFTdata[i] = (double) s[i];
                    }
                    fft.rdft(1, FFTdata);
                    Log.d(Config.DEBUG_KEY, "");

                    double db = 20.0 * Math.log10(maxValue / baseValue);
                    if(mCallback != null){
                        mCallback.onRecord(mRecordingBuffer, db);
                    }
                    Log.d("TAG",String.format("dB: %02.0f",db));
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
