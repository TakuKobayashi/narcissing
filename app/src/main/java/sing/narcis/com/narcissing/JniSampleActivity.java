package sing.narcis.com.narcissing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class JniSampleActivity extends Activity {
    static {
        System.loadLibrary("jni_sample");
    }

    private Camera mCamera;
    private View mPreview;
    //private ImageView mCameraDecodeView;
    private static final int REQUEST_GALLERY = 1;

    private native int[] convert(int[] pixcels,int width, int height);
    private native int[] grayscale(int[] pixcels,int width, int height,int value);
    private native int[] decodeYUV420SP(byte[] yuv,int width, int height);
    private native int[] mosaic(int[] pixcels,int width, int height,int dot);
    private native int[] approximateColor(int[] pixcels,int width, int height,int targetColor, int threshold);
    private native int[] noiseRemove(int[] pixcels,int width, int height);
    private native int[] negative(int[] pixcels,int width, int height);

    private Bitmap mOrigin;
    private VerticalSeekBar mVerticalSeekBar;
    private TextView mSeekbarValue;
    private int currentPosition;
    private FaceOverlayImageView mFaceImage;
    private PointF mTargetPoint;
    private TextView mSelectPixelInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jni_sample);

        mTargetPoint = new PointF(0,0);

        mOrigin = BitmapFactory.decodeResource(getResources(), R.mipmap.face_sample);

        FilterPagerAdapter adapter = new FilterPagerAdapter(this);

        String[] array_str = getResources().getStringArray(R.array.filter_names);
        for (String s : array_str) {
            adapter.add(s);
        }
        ViewPager pager = (ViewPager)findViewById(R.id.filterPager);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentPosition = position;
                if(currentPosition == 2){
                    mVerticalSeekBar.setMax((int) Math.ceil(Math.sqrt(255 * 255 + 255 * 255 + 255 * 255)));
                }else{
                    mVerticalSeekBar.setMax(256);
                }
                filter(currentPosition);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ImageView before = (ImageView) findViewById(R.id.before);
        before.setImageBitmap(mOrigin);
        before.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (currentPosition == 2) {
                    mTargetPoint.x = event.getX();
                    mTargetPoint.y = event.getY();
                    filter(currentPosition);
                }
                return false;
            }
        });

        mSelectPixelInfo = (TextView) findViewById(R.id.selectPixelInfo);

        /*
        before.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ギャラリー呼び出し
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        });
        */

        mSeekbarValue = (TextView) findViewById(R.id.seekBarValue);

        mVerticalSeekBar = (VerticalSeekBar) findViewById(R.id.VerticalSeekBar);
        mVerticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSeekbarValue.setText(String.valueOf(progress));
                filter(currentPosition);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mSeekbarValue.setText(String.valueOf(mVerticalSeekBar.getProgress()));

        mFaceImage = (FaceOverlayImageView) findViewById(R.id.faceImage);
        mFaceImage.faceRecognize(mOrigin);
        //mCameraDecodeView = (ImageView) findViewById(R.id.camera_decode_view);

        //RelativeLayout layout = (RelativeLayout) findViewById(R.id.sampleLayout);
        //RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //layoutParams.addRule(RelativeLayout.BELOW, R.id.filterGrid);
        //layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.spacer);
        //View preview = setupCamera();
        //preview.setLayoutParams(layoutParams);
        //layout.addView(preview);
        filter(currentPosition);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            mOrigin.recycle();
            mOrigin = null;
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                mOrigin = BitmapFactory.decodeStream(in);
                in.close();
                ImageView before = (ImageView) findViewById(R.id.before);
                before.setImageBitmap(mOrigin);
                filter(currentPosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void filter(int position){
        mSelectPixelInfo.setVisibility(View.INVISIBLE);

        Bitmap subbmp = mOrigin.copy(Bitmap.Config.ARGB_8888, true);
        int width = subbmp.getWidth();
        int height = subbmp.getHeight();

        int[] pixels = new int[width * height];
        subbmp.getPixels(pixels, 0, width, 0, 0, width, height);
        if(position == 0){
            pixels = grayscale(pixels, width, height, Math.max(mVerticalSeekBar.getProgress(), 1));
        }else if(position == 1){
            pixels = mosaic(pixels, width, height, Math.max(mVerticalSeekBar.getProgress(), 1));
        }else if(position == 2){
            mSelectPixelInfo.setVisibility(View.VISIBLE);
            int targetColor = pixels[(int)mTargetPoint.x + (int)mTargetPoint.y * width];
            mSelectPixelInfo.setText("x:" + (int)mTargetPoint.x + " y:" + (int)mTargetPoint.y+ " a:" + Color.alpha(targetColor)+ " r:" +Color.red(targetColor)+ " g:"+Color.green(targetColor)+" b:" + Color.blue(targetColor));
            pixels = approximateColor(pixels, width, height, targetColor, Math.max(mVerticalSeekBar.getProgress(), 1));
        }else if(position == 3){
            pixels = noiseRemove(pixels, width, height);
        }else if(position == 4){
            pixels = negative(pixels, width, height);
        }
        subbmp.setPixels(pixels, 0, width, 0, 0, width, height);
        ImageView after = (ImageView) findViewById(R.id.after);
        after.setImageBitmap(subbmp);
        pixels = null;
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback(){

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size previewSize = camera.getParameters().getPreviewSize();
            int[] decoded = decodeYUV420SP(data, previewSize.width, previewSize.height);
            Bitmap bitmap = Bitmap.createBitmap(decoded, previewSize.width, previewSize.height, Bitmap.Config.ARGB_8888);
            //mCameraDecodeView.setImageBitmap(bitmap);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
        ApplicationHelper.releaseImageView((ImageView) findViewById(R.id.before));
        ApplicationHelper.releaseImageView((ImageView) findViewById(R.id.after));
        ApplicationHelper.releaseImageView(mFaceImage);
        //ApplicationHelper.releaseImageView(mCameraDecodeView);
    }
}
