package sing.narcis.com.narcissing;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class ImageLoader extends ContextSingletonBase<ImageLoader> {

    protected long currentId = 0;
    protected HashMap<Long, String> fileList;
    protected ArrayList<Bitmap> loadingImages;
    protected boolean isLoaded = false;

    public void init(Context context) {
        super.init(context);
        fileList = new HashMap<Long, String>();
        loadingImages = new ArrayList<Bitmap>();
    }

    public void loadingClear(){
        for(Bitmap bitmap : loadingImages) {
            bitmap.recycle();
        }
        loadingImages.clear();
    }

    //画像を読み込んで実際にメモリに乗っける
    public abstract void preLoad();

    public void asynkPreLoad(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                preLoad();
            }
        }).start();
    }

    public ArrayList<Bitmap> getLoadingImages(){
        return loadingImages;
    }

    public ArrayList<Bitmap> loadImages(){
        preLoad();
        return loadingImages;
    }

    public ArrayList<Long> setImageAssetPathes(String... pathes) {
        ArrayList<Long> idList = new ArrayList<Long>();
        for(String path : pathes){
            ++currentId;
            fileList.put(currentId, path);
            idList.add(currentId);
        }
        return idList;
    }

    public void release() {
        loadingClear();
        fileList.clear();
    }

    public boolean IsLoaded(){
        return isLoaded;
    }

    //デストラクタ
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }
}
