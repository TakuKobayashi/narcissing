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
        AssetManager mngr = context.getAssets();
        for (Map.Entry<Long, String> e : fileList.entrySet()) {
            try {
                String[] pathes = mngr.list(e.getValue());
                if(pathes.length > 0) {
                    for (String path : pathes) {
                        InputStream is = mngr.open(e.getValue() + "/" + path);
                        loadingImages.add(BitmapFactory.decodeStream(is));
                    }
                }else {
                    InputStream is = mngr.open(e.getValue());
                    loadingImages.add(BitmapFactory.decodeStream(is));
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public ArrayList<Bitmap> getLoadingImages(){
        return loadingImages;
    }

    public ArrayList<Bitmap> loadingImages(){
        preLoad();
        return loadingImages;
    }

    public ArrayList<Long> setImageAssetPathes(String... pathes) {
        ArrayList<Long> idList = new ArrayList<Long>();
        for(String path : pathes){
            currentId += 1;
            fileList.put(currentId, path);
            idList.add(currentId);
        }
        return idList;
    }
}
