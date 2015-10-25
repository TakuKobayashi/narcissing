package sing.narcis.com.narcissing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class FrameAnimationRenderer{

  private ArrayList<Bitmap> mAnimationImages;
  private int mCurrentFrameIndex = 0;
  private int mFrameCount = 0;

  public FrameAnimationRenderer() {
    mAnimationImages = new ArrayList<Bitmap>();
  }

  public void render(Canvas canvas){
    if(mAnimationImages.size() <= mCurrentFrameIndex){
      stopAnimation();
      return;
    }
    ++mFrameCount;
    Bitmap frame = mAnimationImages.get(mCurrentFrameIndex);
    Rect src = new Rect(0,0,frame.getWidth(), frame.getHeight());
    RectF dst = new RectF(Math.max((float) (canvas.getWidth() - frame.getWidth()) / 2, 0), Math.max((float)(canvas.getHeight() - frame.getHeight()) / 2, 0), Math.min((float) (canvas.getWidth() + frame.getWidth()) / 2, canvas.getWidth()), Math.min((float) (canvas.getHeight() + frame.getHeight()) / 2, canvas.getHeight()));
    canvas.drawBitmap(mAnimationImages.get(mCurrentFrameIndex), src, dst, null);
    if(mFrameCount % 5 != 0) return;
    ++mCurrentFrameIndex;
    if(mAnimationImages.size() <= mCurrentFrameIndex){
      stopAnimation();
    }
  }

  public void stopAnimation(){
    mAnimationImages.clear();
    mCurrentFrameIndex = 0;
    mFrameCount = 0;
  }

  public void startAnimation(){
    if(mAnimationImages.size() > 0) return;
    mAnimationImages = AssetImageLoader.getInstance(AssetImageLoader.class).getLoadingImages();
  }
}