package sing.narcis.com.narcissing;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.xml.sax.helpers.LocatorImpl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

public class CameraOverrideView extends View {

  private Bitmap _clearImage = null;
  private Bitmap _renderBaseImage = null;
  private FrameAnimationRenderer mRenderer;

  public CameraOverrideView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mRenderer = new FrameAnimationRenderer();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if(_renderBaseImage != null){
      _renderBaseImage.recycle();
      _renderBaseImage = null;
    }
    _renderBaseImage = _clearImage.copy(Bitmap.Config.ARGB_8888, true);
    Canvas bitmapCanvas = new Canvas(_renderBaseImage);
    mRenderer.render(bitmapCanvas);
    canvas.drawBitmap(_renderBaseImage, null, new Rect(0,0,_renderBaseImage.getWidth(), _renderBaseImage.getHeight()), null);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    if(_clearImage != null){
      _clearImage.recycle();
      _clearImage = null;
    }
    _clearImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    this.invalidate();
  }

  public void startAnimation(){
    mRenderer.startAnimation();
  }

  public void releaseAllImage(){
    mRenderer.stopAnimation();
    if(_clearImage != null){
      _clearImage.recycle();
      _clearImage = null;
    }
    if(_renderBaseImage != null){
      _renderBaseImage.recycle();
      _renderBaseImage = null;
    }
  }
}