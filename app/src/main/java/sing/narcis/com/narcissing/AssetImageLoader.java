package sing.narcis.com.narcissing;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AssetImageLoader extends ImageLoader {

    //画像を読み込んで実際にメモリに乗っける
    public void preLoad(){
        loadingClear();
        isLoaded = false;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        AssetManager mngr = context.getAssets();
        for (Map.Entry<Long, String> e : fileList.entrySet()) {
            try {
                String[] pathes = mngr.list(e.getValue());
                if(pathes.length > 0) {
                    for (String path : pathes) {
                        InputStream is = mngr.open(e.getValue() + "/" + path);
                        loadingImages.add(BitmapFactory.decodeStream(is, null, options));
                    }
                }else {
                    InputStream is = mngr.open(e.getValue());
                    loadingImages.add(BitmapFactory.decodeStream(is, null, options));
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        isLoaded = true;
    }
}
