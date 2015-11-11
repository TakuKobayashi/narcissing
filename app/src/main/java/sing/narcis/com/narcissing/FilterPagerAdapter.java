package sing.narcis.com.narcissing;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class FilterPagerAdapter extends PagerAdapter {

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * リスト.
     */
    private ArrayList<String> mList;

    /**
     * コンストラクタ.
     */
    public FilterPagerAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<String>();
    }

    /**
     * リストにアイテムを追加する.
     *
     * @param item アイテム
     */
    public void add(String item) {
        mList.add(item);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        // リストから取得
        String item = mList.get(position);

        // View を生成
        TextView textView = new TextView(mContext);
        textView.setText(item);
        textView.setTextSize(24);
        textView.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        // コンテナに追加
        container.addView(textView);

        return textView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // コンテナから View を削除
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        // リストのアイテム数を返す
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        // Object 内に View が存在するか判定する
        return view == (TextView) object;
    }
}