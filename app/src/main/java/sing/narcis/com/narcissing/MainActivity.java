package sing.narcis.com.narcissing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {
    private static int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        requestPermission();
    }

    private void requestPermission(){
        if(Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissions = ApplicationHelper.getSettingPermissions(this);
            boolean isRequestPermission = false;
            for(String permission : permissions){
                if(!ApplicationHelper.hasSelfPermission(this, permission)){
                    isRequestPermission = true;
                    break;
                }
            }
            if(isRequestPermission) {
                requestPermissions(permissions.toArray(new String[0]), REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioRecordThread.getInstance(AudioRecordThread.class).stopRecording();
        SensorStreamer.getInstance(SensorStreamer.class).release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.button)
    public void onClickButton(View v) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button2)
    public void onClickButton2(View v) {
        Intent intent = new Intent(this, SoundActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button6)
    public void onClickButton6(View v) {
        Intent intent = new Intent(this, Camera2Activity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button7)
    public void onClickButton7(View v) {
        Intent intent = new Intent(this, JniSampleActivity.class);
        startActivity(intent);
    }
}
