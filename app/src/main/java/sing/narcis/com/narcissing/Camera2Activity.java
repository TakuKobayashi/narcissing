package sing.narcis.com.narcissing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class Camera2Activity extends Activity {

    private CameraOverrideView mCameraOverrideView;
    private AudioTrack audioTrack;
    private int headCount = 0;
    private TextureView mPreview;
    private Size mPreviewSize;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraDevice mCameraDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera2);
        playFromAudioTrack();
        mCameraOverrideView = (CameraOverrideView) findViewById(R.id.camera_override_view2);
        AssetImageLoader.getInstance(AssetImageLoader.class).asynkPreLoad();

        SensorStreamer.getInstance(SensorStreamer.class).setOnSensorStreamCallback(new SensorStreamer.SensorStreamCallback() {
            @Override
            public void onSenssing(float x, float y, float z) {
                float sum = Math.abs(x) + Math.abs(y) + Math.abs(z);
                if (sum > 1.05) {
                    headCount++;
                }
                if (headCount > 80) {
                    mCameraOverrideView.startAnimation();
                    headCount = 0;
                }
            }
        });
        mPreview = (TextureView) findViewById(R.id.camera_preview2);
        mPreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(Config.DEBUG_KEY, "available width:" + width + " height:" + height + " time:" + surface.getTimestamp());
                setupCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.d(Config.DEBUG_KEY, "changed width:" + width + " height:" + height + " time:" + surface.getTimestamp());
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.d(Config.DEBUG_KEY, "destroyed time:" + surface.getTimestamp());
                return false;
            }

            //毎フレーム呼ばれる
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                //getTimeStampは更新されたナノ秒単位の値を取得
                Log.d(Config.DEBUG_KEY, "updated time:" + surface.getTimestamp());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraOverrideView.releaseAllImage();
        releaseAudio();
    }

    private void playFromAudioTrack() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 音データを読み込む
                    AssetFileDescriptor assetFileDescriptor = Camera2Activity.this.getAssets().openFd("audio/narsing_s.wav");
                    byte[] byteData = new byte[(int) assetFileDescriptor.getLength()];
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
                    releaseAudio();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void releaseAudio() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.flush();
            audioTrack = null;
        }
    }

    private void releaseCamera(){
        if(mCameraDevice != null){
            mCameraDevice.close();
        }
    }

    public void onConfigurationChanged(Configuration newConfig)
    {
        // 画面の回転・サイズ変更でプレビュー画像の向きを変更する.
        super.onConfigurationChanged(newConfig);
        mPreview.setTransform(configureTransform(mPreviewSize));
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("ResourceType")
    private void setupCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        // Back Cameraを取得してOpen.
        try {
            for (String strCameraId : manager.getCameraIdList()) {
                Log.d(Config.DEBUG_KEY, "cameraId:" + strCameraId);
                // Cameraから情報を取得するためのCharacteristics.
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(strCameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    // Front Cameraならスキップ.
                    continue;
                }
                // ストリームの設定を取得(出力サイズを取得する).
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                // TODO: 配列から最大の組み合わせを取得する.
                Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
                for (Size size : sizes) {
                    Log.d(Config.DEBUG_KEY, "supportWidth:" + size.getWidth() + " supportHeight:" + size.getHeight());
                }

                mPreviewSize = sizes[0];
                mPreview.setTransform(configureTransform(mPreviewSize));

                manager.openCamera(strCameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(CameraDevice camera) {
                        mCameraDevice = camera;
                        SurfaceTexture texture = mPreview.getSurfaceTexture();
                        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                        Surface surface = new Surface(texture);
                        try {
                            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            mPreviewBuilder.addTarget(surface);
                            camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                                @Override
                                public void onConfigured(CameraCaptureSession session) {
                                    // オートフォーカスモードに設定する.
                                    mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                    // 別スレッドで実行.
                                    HandlerThread thread = new HandlerThread("CameraPreview");
                                    thread.start();
                                    Handler backgroundHandler = new Handler(thread.getLooper());

                                    try {
                                        // 画像を繰り返し取得してTextureViewに表示する.
                                        session.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
                                    } catch (CameraAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onConfigureFailed(CameraCaptureSession session) {
                                    Log.d(Config.DEBUG_KEY, "configulation failed");
                                }
                            }, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onDisconnected(CameraDevice cmdCamera) {
                        cmdCamera.close();
                        mCameraDevice = null;
                        Log.d(Config.DEBUG_KEY, "disConnected");
                    }

                    @Override
                    public void onError(CameraDevice cmdCamera, int error) {
                        cmdCamera.close();
                        mCameraDevice = null;
                        Log.d(Config.DEBUG_KEY, "error:" + error);
                    }
                }, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Matrix configureTransform(Size previewSize)
    {
        // 画面の回転に合わせてmTextureViewの向き、サイズを変更する.
        Display dsply = getWindowManager().getDefaultDisplay();

        int rotation = dsply.getRotation();
        Matrix matrix = new Matrix();

        Point pntDisplay = new Point();
        dsply.getSize(pntDisplay);

        RectF rctView = new RectF(0, 0, pntDisplay.x, pntDisplay.y);
        RectF rctPreview = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = rctView.centerX();
        float centerY = rctView.centerY();

        rctPreview.offset(centerX - rctPreview.centerX(), centerY - rctPreview.centerY());
        matrix.setRectToRect(rctView, rctPreview, Matrix.ScaleToFit.FILL);
        float scale = Math.max(
                (float) rctView.width() / previewSize.getWidth(),
                (float) rctView.height() / previewSize.getHeight()
        );
        matrix.postScale(scale, scale, centerX, centerY);

        switch (rotation) {
            case Surface.ROTATION_0:
                matrix.postRotate(0, centerX, centerY);
                break;
            case Surface.ROTATION_90:
                matrix.postRotate(270, centerX, centerY);
                break;
            case Surface.ROTATION_180:
                matrix.postRotate(180, centerX, centerY);
                break;
            case Surface.ROTATION_270:
                matrix.postRotate(90, centerX, centerY);
                break;
        }
        return matrix;
    }
}
