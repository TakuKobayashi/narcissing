package sing.narcis.com.narcissing;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class ImageLoader extends ContextSingletonBase<ImageLoader> {

    protected long currentId = 0;
    protected HashMap<Long, String> fileList;
    protected ArrayList<Bitmap> loadingImages;

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

    public void release() {
        loadingClear();
        fileList.clear();
    }

    //デストラクタ
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }
}
