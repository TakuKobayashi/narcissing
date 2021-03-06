package sing.narcis.com.narcissing;

import android.Manifest;
import android.app.Application;

public class NarcissingApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    if(ApplicationHelper.hasSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
      AudioRecordThread.getInstance(AudioRecordThread.class).init(this);
      //AudioRecordThread.getInstance(AudioRecordThread.class).startRecording();
    }
    AssetImageLoader.getInstance(AssetImageLoader.class).init(this);
    AssetImageLoader.getInstance(AssetImageLoader.class).setImageAssetPathes("frame_png");
    SensorStreamer.getInstance(SensorStreamer.class).init(this);
    SensorStreamer.getInstance(SensorStreamer.class).startSenssing();
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }
}
