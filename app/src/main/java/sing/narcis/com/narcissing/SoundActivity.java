package sing.narcis.com.narcissing;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import bg.cytec.android.fskmodem.FSKConfig;
import bg.cytec.android.fskmodem.FSKDecoder;
import bg.cytec.android.fskmodem.FSKEncoder;
import butterknife.ButterKnife;
import butterknife.OnClick;

/*
VIE SHARE
#define BAUD  2450
#define FREQ_LOW  17680
#define FREQ_HIGH 23520
 */

public class SoundActivity extends Activity {
    private AudioTrack mAudioTrack;
    protected FSKConfig mConfig;
    protected FSKEncoder mEncoder;
    protected FSKDecoder mDecoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sound);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.sound1)
    public void onClickSound1(View v) {
        Toast.makeText(SoundActivity.this, "1", Toast.LENGTH_SHORT).show();

        try {
            mConfig = new FSKConfig(FSKConfig.SAMPLE_RATE_44100, FSKConfig.PCM_8BIT, FSKConfig.CHANNELS_MONO, FSKConfig.SOFT_MODEM_MODE_9, FSKConfig.THRESHOLD_20P);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        /// INIT FSK ENCODER
        mEncoder = new FSKEncoder(mConfig, new FSKEncoder.FSKEncoderCallback() {

            @Override
            public void encoded(byte[] pcm8, short[] pcm16) {
                if (mConfig.pcmFormat == FSKConfig.PCM_8BIT) {
                    //8bit buffer is populated, 16bit buffer is null

                    mAudioTrack.write(pcm8, 0, pcm8.length);

                    mDecoder.appendSignal(pcm8);
                }
                else if (mConfig.pcmFormat == FSKConfig.PCM_16BIT) {
                    //16bit buffer is populated, 8bit buffer is null

                    mAudioTrack.write(pcm16, 0, pcm16.length);

                    mDecoder.appendSignal(pcm16);
                }
            }
        });

        ///

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mConfig.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT, 1024,
                AudioTrack.MODE_STREAM);

        mAudioTrack.play();


    }

    @OnClick(R.id.sound2)
    public void onClickSound2(View v) {
        Toast.makeText(SoundActivity.this, "2", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.sound3)
    public void onClickSound3(View v) {
        Toast.makeText(SoundActivity.this, "3", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.sound4)
    public void onClickSound4(View v) {
        Toast.makeText(SoundActivity.this, "4", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.sound5)
    public void onClickSound5(View v) {
        Toast.makeText(SoundActivity.this, "5", Toast.LENGTH_SHORT).show();
    }


}
