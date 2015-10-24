package sing.narcis.com.narcissing;

import android.app.Application;

public class NarcissingApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    AudioRecordThread.getInstance(AudioRecordThread.class).init(this);
    AudioRecordThread.getInstance(AudioRecordThread.class).startRecording();
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }
}
