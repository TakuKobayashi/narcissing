package sing.narcis.com.narcissing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.IOException;

public class JniSampleActivity extends Activity {
    static {
        System.loadLibrary("jni_sample");
    }

    private Camera mCamera;
    private View mPreview;
    private ImageView mCameraDecodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jni_sample);

        Bitmap origin = BitmapFactory.decodeResource(getResources(), R.mipmap.jni_sample);
        Bitmap subbmp = origin.copy(Bitmap.Config.ARGB_8888, true);

        int width = subbmp.getWidth();
        int height = subbmp.getHeight();

        int pixels[] = new int[width * height];
        int cnt = 0;
        subbmp.getPixels(pixels, 0, width, 0, 0, width, height);
        pixels = convert(pixels, width, height);
        subbmp.setPixels(pixels, 0, width, 0, 0, width, height);

        ImageView before = (ImageView) findViewById(R.id.before);
        before.setImageBitmap(origin);

        ImageView after = (ImageView) findViewById(R.id.after);
        after.setImageBitmap(subbmp);

        mCameraDecodeView = (ImageView) findViewById(R.id.camera_decode_view);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.sampleLayout);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.spacer);
        layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.spacer);
        View preview = setupCamera();
        preview.setLayoutParams(layoutParams);
        layout.addView(preview);
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback(){

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size previewSize = camera.getParameters().getPreviewSize();
            int[] decoded = decodeYUV420SP(data, previewSize.width, previewSize.height);
            Bitmap bitmap = Bitmap.createBitmap(decoded, previewSize.width, previewSize.height, Bitmap.Config.ARGB_8888);
            mCameraDecodeView.setImageBitmap(bitmap);
            decoded = null;
        }
    };

    private View setupCamera(){
        if(Build.VERSION.SDK_INT >= 14) {
            TextureView preview = new TextureView(this);
            preview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    Log.d(Config.DEBUG_KEY, "available");
                    try {
                        mCamera = Camera.open(); // attempt to get a Camera instance
                        mCamera.setPreviewTexture(surface);
                        setupCameraParams(mCamera);
                    } catch (Exception e) {
                        // Camera is not available (in use or does not exist)
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    Log.d(Config.DEBUG_KEY, "changed");
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    releaseCamera();
                    Log.d(Config.DEBUG_KEY, "destroyed");
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    Log.d(Config.DEBUG_KEY, "updated");
                }
            });
            mPreview = preview;
            return preview;
        }else{
            SurfaceView surefaceView = new SurfaceView(this);
            SurfaceHolder holder = surefaceView.getHolder();
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
            mCamera.setPreviewCallback(mPreviewCallback);
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            setupCameraParams(mCamera);
            mPreview = surefaceView;
            return surefaceView;
        }
    }

    private void setupCameraParams(Camera camera){
        camera.setPreviewCallback(mPreviewCallback);
        Camera.Parameters cp = camera.getParameters();
        //今回はフロントカメラのみなのでCameraIdは0のみ使う
        camera.setDisplayOrientation(ApplicationHelper.getCameraDisplayOrientation(this, 0));
        camera.setParameters(cp);
        camera.startPreview();
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private native int[] convert(int[] pixcels,int width, int height);
    private native int[] decodeYUV420SP(byte[] yuv,int width, int height);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        ApplicationHelper.releaseImageView((ImageView) findViewById(R.id.before));
        ApplicationHelper.releaseImageView((ImageView) findViewById(R.id.after));
        ApplicationHelper.releaseImageView(mCameraDecodeView);
    }
}
