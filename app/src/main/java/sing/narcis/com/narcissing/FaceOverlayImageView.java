package sing.narcis.com.narcissing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.HashMap;
import java.util.Map;

public class FaceOverlayImageView extends ImageView {
  private HashMap<String, RectF> mCaptured;
  private Paint mPaint;

  public FaceOverlayImageView(Context context) {
    super(context);
    setup();
  }

  public FaceOverlayImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setup();
  }

  public FaceOverlayImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setup();
  }

  private void setup(){
    mCaptured = new HashMap<String,RectF>();
    mCaptured.put("hogehoge", new RectF(100,100,200,200));
    mPaint = new Paint();
    mPaint.setColor(Color.RED);
    mPaint.setStyle(Paint.Style.STROKE);
  }

  public void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(h, w, oldh, oldw);
  }

  @Override
  protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(heightMeasureSpec, widthMeasureSpec);
    setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
  }

  protected void onDraw(Canvas c) {
    super.onDraw(c);
    for(Map.Entry<String, RectF> kr : mCaptured.entrySet()){
      c.drawRect(kr.getValue(), mPaint);
    }
  }

  public void faceRecognize(Bitmap bitmap){
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();

    int[] pixels = new int[width * height];
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

    setImageBitmap(bitmap);
    this.invalidate();
  }

  public HashMap<String, RectF> getCaptured(){
    return mCaptured;
  }
}
