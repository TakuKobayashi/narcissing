package sing.narcis.com.narcissing;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;

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
    static String ENCODER_DATA;

    private AudioTrack mAudioTrack;
    protected FSKConfig mConfig;
    protected FSKEncoder mEncoder;
    protected FSKDecoder mDecoder;

    protected Runnable mDataFeeder = new Runnable() {

        @Override
        public void run() {
            byte[] data = ENCODER_DATA.getBytes();

            if (data.length > FSKConfig.ENCODER_DATA_BUFFER_SIZE) {
                //chunk data

                byte[] buffer = new byte[FSKConfig.ENCODER_DATA_BUFFER_SIZE];

                ByteBuffer dataFeed = ByteBuffer.wrap(data);

                while (dataFeed.remaining() > 0) {

                    if (dataFeed.remaining() < buffer.length) {
                        buffer = new byte[dataFeed.remaining()];
                    }

                    dataFeed.get(buffer);

                    mEncoder.appendData(buffer);

                    try {
                        Thread.sleep(100); //wait for encoder to do its job, to avoid buffer overflow and data rejection
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                mEncoder.appendData(data);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sound);
        ButterKnife.bind(this);
        //AudioRecordThreadからマイクから拾った音を受け取る場合はこのような感じでCallbackを設定すると値が取得できる
        AudioRecordThread.getInstance(AudioRecordThread.class).setOnAudioRecordCallback(new AudioRecordThread.AudioRecordCallback() {
            @Override
            public void onRecord(byte[] raw, double decibel) {
                Log.d(Config.DEBUG_KEY, "rawL:" + raw.length + " db:" + decibel);
            }
        });

        try {
            mConfig = new FSKConfig(FSKConfig.SAMPLE_RATE_44100, FSKConfig.PCM_8BIT, FSKConfig.CHANNELS_MONO, FSKConfig.SOFT_MODEM_MODE_9, FSKConfig.THRESHOLD_1P);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        /// INIT FSK DECODER

        mDecoder = new FSKDecoder(mConfig, new FSKDecoder.FSKDecoderCallback() {

            @Override
            public void decoded(byte[] newData) {

                final String text = new String(newData);

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(SoundActivity.this, "run()", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        /// INIT FSK ENCODER

        mEncoder = new FSKEncoder(mConfig, new FSKEncoder.FSKEncoderCallback() {

            @Override
            public void encoded(byte[] pcm8, short[] pcm16) {
                if (mConfig.pcmFormat == FSKConfig.PCM_8BIT) {
                    //8bit buffer is populated, 16bit buffer is null

                    for (int i = 0; i < 20; i++) {
                        mAudioTrack.write(pcm8, 0, pcm8.length);
                        try {
                            Thread.sleep(30); //wait for encoder to do its job, to avoid buffer overflow and data rejection
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    mDecoder.appendSignal(pcm8);
                    enableButton();
                } else if (mConfig.pcmFormat == FSKConfig.PCM_16BIT) {
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

    private void enableButton() {
        setButtonEnable(true);
    }

    public void setButtonEnable(final boolean isEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((Button) findViewById(R.id.sound1)).setEnabled(isEnabled);
                ((Button) findViewById(R.id.sound2)).setEnabled(isEnabled);
                ((Button) findViewById(R.id.sound3)).setEnabled(isEnabled);
                ((Button) findViewById(R.id.sound4)).setEnabled(isEnabled);
                ((Button) findViewById(R.id.sound5)).setEnabled(isEnabled);
                ((Button) findViewById(R.id.sound6)).setEnabled(isEnabled);
            }
        });
    }


    private void disableButton() {
        setButtonEnable(false);
    }

    @Override
    protected void onDestroy() {
        mDecoder.stop();

        mEncoder.stop();

        mAudioTrack.stop();
        mAudioTrack.release();

        super.onDestroy();
    }


    @OnClick(R.id.sound1)
    public void onClickSound1(View v) {
        setButtonEnable(false);
        ENCODER_DATA = "r";
        Toast.makeText(SoundActivity.this, ENCODER_DATA, Toast.LENGTH_SHORT).show();
        new Thread(mDataFeeder, ENCODER_DATA).start();

    }

    @OnClick(R.id.sound2)
    public void onClickSound2(View v) {
        setButtonEnable(false);
        ENCODER_DATA = "g";
        Toast.makeText(SoundActivity.this, ENCODER_DATA, Toast.LENGTH_SHORT).show();
        new Thread(mDataFeeder, ENCODER_DATA).start();
    }

    @OnClick(R.id.sound3)
    public void onClickSound3(View v) {
        setButtonEnable(false);
        ENCODER_DATA = "b";
        Toast.makeText(SoundActivity.this, ENCODER_DATA, Toast.LENGTH_SHORT).show();
        new Thread(mDataFeeder, ENCODER_DATA).start();
    }

    @OnClick(R.id.sound4)
    public void onClickSound4(View v) {
        setButtonEnable(false);
        ENCODER_DATA = "y";
        Toast.makeText(SoundActivity.this, ENCODER_DATA, Toast.LENGTH_SHORT).show();
        new Thread(mDataFeeder, ENCODER_DATA).start();
    }

    @OnClick(R.id.sound5)
    public void onClickSound5(View v) {
        setButtonEnable(false);
        ENCODER_DATA = "w";
        Toast.makeText(SoundActivity.this, ENCODER_DATA, Toast.LENGTH_SHORT).show();
        new Thread(mDataFeeder, ENCODER_DATA).start();
    }

    @OnClick(R.id.sound6)
    public void onClickSound6(View v) {
        setButtonEnable(false);
        ENCODER_DATA = "c";
        Toast.makeText(SoundActivity.this, ENCODER_DATA, Toast.LENGTH_SHORT).show();
        new Thread(mDataFeeder, ENCODER_DATA).start();
    }


    @OnClick(R.id.sound_red)
    public void onClickSoundRed(View v) {
        startViewLedIntentService(VieLedIntentService.RED);
    }

    @OnClick(R.id.sound_green)
    public void onClickSoundGreen(View v) {
        startViewLedIntentService(VieLedIntentService.GREEN);
    }
    @OnClick(R.id.sound_blue)
    public void onClickSoundBlue(View v) {
        startViewLedIntentService(VieLedIntentService.BLUE);
    }
    @OnClick(R.id.sound_yellow)
    public void onClickSoundYellow(View v) {
        startViewLedIntentService(VieLedIntentService.YELLOW);
    }
    @OnClick(R.id.sound_white)
    public void onClickSoundWhite(View v) {
        startViewLedIntentService(VieLedIntentService.WHITE);
    }
    @OnClick(R.id.sound_clear)
    public void onClickSoundClear(View v) {
        startViewLedIntentService(VieLedIntentService.CLEAR);
    }

    private void startViewLedIntentService(final String color) {
        Intent intent = new Intent(this, VieLedIntentService.class);
        intent.putExtra(VieLedIntentService.EXTRA_COLOR, color);
        startService(intent);
    }


}
