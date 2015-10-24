package sing.narcis.com.narcissing;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SoundActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sound);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.sound1)
    public void onClickSound1(View v) {
        Toast.makeText(SoundActivity.this, "1", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.sound2)
    public void onClickSound2(View v) {
        Toast.makeText(SoundActivity.this, "2", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.sound3)
    public void onClickSound3(View v) {
        Toast.makeText(SoundActivity.this, "3", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.sound4)
    public void onClickSound4(View v) {
        Toast.makeText(SoundActivity.this, "4", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.sound5)
    public void onClickSound5(View v) {
        Toast.makeText(SoundActivity.this, "5", Toast.LENGTH_SHORT).show();
    }


}
