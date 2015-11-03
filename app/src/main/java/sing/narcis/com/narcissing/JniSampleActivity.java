package sing.narcis.com.narcissing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class JniSampleActivity extends Activity {
    static {
        System.loadLibrary("jni_sample");
    }

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
        convert(pixels, width, height);
        subbmp.setPixels(pixels, 0, width, 0, 0, width, height);

        ImageView before = (ImageView) findViewById(R.id.before);
        before.setImageBitmap(origin);

        ImageView after = (ImageView) findViewById(R.id.after);
        after.setImageBitmap(subbmp);
    }

    private native void convert(int[] pixcels,int width, int height);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApplicationHelper.releaseImageView((ImageView) findViewById(R.id.before));
        ApplicationHelper.releaseImageView((ImageView) findViewById(R.id.after));
    }
}
