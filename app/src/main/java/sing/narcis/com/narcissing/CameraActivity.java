package sing.narcis.com.narcissing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;


public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraOverrideView mCameraOverrideView;
    private Timer mTimer;
    private String[] colors = new String[]{VieLedIntentService.WHITE, VieLedIntentService.BLUE, VieLedIntentService.GREEN, VieLedIntentService.RED, VieLedIntentService.YELLOW};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        playFromAudioTrack();
        mCameraOverrideView = (CameraOverrideView) findViewById(R.id.camera_override_view);
        AssetImageLoader.getInstance(AssetImageLoader.class).asynkPreLoad();
        mCameraOverrideView.startAnimation();

        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                String color = colors[(int)(Math.random() * colors.length)];
                Intent intent = new Intent(CameraActivity.this, VieLedIntentService.class);
                intent.putExtra(VieLedIntentService.EXTRA_COLOR, color);
                startService(intent);
            }
        };
        mTimer.schedule(task, 0, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraOverrideView.releaseAllImage();
        mTimer.cancel();
    }

    private void playFromAudioTrack() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 音データを読み込む
                    AssetFileDescriptor assetFileDescriptor = CameraActivity.this.getAssets().openFd("audio/narsing_s.wav");
                    byte[] byteData = new byte[(int)assetFileDescriptor.getLength()];
                    InputStream is = assetFileDescriptor.createInputStream();
                    is.read(byteData);
                    is.close();
                    // バッファサイズを取得
                    int bufSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                    // AudioTrackインスタンスを生成
                    AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);
                    // 再生
                    audioTrack.play();
                    audioTrack.write(byteData, 46, byteData.length - 46);
                    audioTrack.stop();
                    audioTrack.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setupCamera(){
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            return;
        }
        SurfaceView preview = (SurfaceView) findViewById(R.id.camera_preview);
        SurfaceHolder holder = preview.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mCamera != null) {
                    try {
                        mCamera.setPreviewDisplay(holder);
                    } catch (IOException exception) {
                        releaseCamera();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mCamera != null) {
                    try {
                        mCamera.setPreviewDisplay(holder);
                    } catch (IOException exception) {
                        releaseCamera();
                    }
                }
            }
        });
        if(Build.VERSION.SDK_INT < 11){
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera.stopPreview();
        //今回はフロントカメラのみなのでCameraIdは0のみ使う
        mCamera.setDisplayOrientation(ApplicationHelper.getCameraDisplayOrientation(this, 0));
        mCamera.startPreview();
    }


    private void releaseCamera(){
        if (mCamera != null){
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        };
    }
}
