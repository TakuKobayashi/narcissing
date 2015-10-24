package sing.narcis.com.narcissing;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioRecordThread extends ContextSingletonBase<AudioRecordThread> {
    private static final int SAMPLING_RATE = 44100;
    private byte[] mRecordingBuffer;
    private AudioRecord mAudioRecord = null;
    private boolean bIsRecording;

    public void init(Context context){
        super.init(context);
    }

    public void startRecording(){
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
                    mAudioRecord.read(mRecordingBuffer, 0, mRecordingBuffer.length);
                    Log.d(Config.DEBUG_KEY, "buffered:" + mRecordingBuffer.length);
                }
                mAudioRecord.stop();
            }
        }).start();
    }

    public void stopRecording() {
        bIsRecording = false;
    }

    //デストラクタ
    @Override
    protected void finalize() throws Throwable {
        stopRecording();
        super.finalize();
    }
}
