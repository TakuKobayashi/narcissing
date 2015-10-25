package sing.narcis.com.narcissing;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorStreamer extends ContextSingletonBase<SensorStreamer> implements SensorEventListener {

    private SensorManager mSensorManager;
    private SensorStreamCallback mCallback;

    public void init(Context context) {
        super.init(context);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void startSenssing(){
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void release() {
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    public void setOnSensorStreamCallback(SensorStreamCallback callback) {
        mCallback = callback;
    }

    public void removeOnSensorStreamCallback() {
        mCallback = null;
    }

    //デストラクタ
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if(mCallback != null){
                mCallback.onSenssing(event.values[0], event.values[1], event.values[2]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface SensorStreamCallback {
        public void onSenssing(float x, float y, float z);
    }
}
