package sing.narcis.com.narcissing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class FrameAnimationRenderer{

  public final static int ANIMATION_TYPE_LOOP = 0;
  public final static int ANIMATION_TYPE_STAY = 1;
  public final static int ANIMATION_TYPE_VARNISH = 2;
  private ArrayList<Bitmap> mAnimationImages;
  private int mCurrentFrameIndex = 0;
  private long mFrameCount = 0;
  private Paint mPaint;
  private int mAnimationType = ANIMATION_TYPE_LOOP;
  private int mLateFrameSpan = 1;

  public FrameAnimationRenderer() {
    mAnimationImages = new ArrayList<Bitmap>();
    mPaint = new Paint();
    mPaint.setFilterBitmap(true);
  }

  public void setLateFrameSpan(int span){
    mLateFrameSpan = span + 1;
  }

  public void setAnimationType(int animationType){
    mAnimationType = animationType;
  }

  public void render(Canvas canvas){
    if(mAnimationImages.isEmpty()) return;
    if(!AssetImageLoader.getInstance(AssetImageLoader.class).IsLoaded()) return;
    ++mFrameCount;
    Bitmap frame = mAnimationImages.get(mCurrentFrameIndex);
    Rect src = new Rect(0,0,frame.getWidth(), frame.getHeight());
    RectF dst = calcScreenField(new Rect(0,0,canvas.getWidth(), canvas.getHeight()), new Rect(0,0,frame.getWidth(), frame.getHeight()));
    canvas.drawBitmap(mAnimationImages.get(mCurrentFrameIndex), src, dst, mPaint);
    if(mFrameCount % mLateFrameSpan != 0) return;
    if(mAnimationType == ANIMATION_TYPE_LOOP){
      if(mAnimationImages.size() - 1 == mCurrentFrameIndex) {
        mCurrentFrameIndex = 0;
      }else {
        ++mCurrentFrameIndex;
      }
    }else if(mAnimationType == ANIMATION_TYPE_STAY){
      if(mAnimationImages.size() - 1 > mCurrentFrameIndex) {
        ++mCurrentFrameIndex;
      }
    }else{
      if(mAnimationImages.size() - 1 == mCurrentFrameIndex) {
        stopAnimation();
      }else{
        ++mCurrentFrameIndex;
      }
    }
  }

  private RectF calcScreenField(Rect screenSize, Rect imageSize){
    float aspectRatio = ((float) screenSize.width()) / screenSize.height();
    float imageRatio = ((float) imageSize.width()) / imageSize.height();
    int width = 0;
    int height = 0;

    // 縦長の解像度端末
    if (imageRatio > aspectRatio) {
      height = screenSize.height();
      width = (int)(height * imageSize.width() / imageSize.height());
    } else if (imageRatio < aspectRatio) {
      width = screenSize.width();
      height = (int)(width * imageSize.height() / imageSize.width());
    } else {
      width = screenSize.width();
      height = screenSize.height();
    }
    int paddingWidth = (screenSize.width() - width) / 2;
    int paddingHeight = (screenSize.height() - height) / 2;
    return new RectF(paddingWidth,paddingHeight,width + paddingWidth,height + paddingHeight);
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