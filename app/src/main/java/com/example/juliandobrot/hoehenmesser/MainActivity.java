package com.example.juliandobrot.hoehenmesser;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class handles the App lifecycle of the Barometric Altimeter.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensorPressure;
    private double hPa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mSensorManager.registerListener(this, mSensorPressure, SensorManager.SENSOR_DELAY_NORMAL);

        hPa = getHPa();

        if(hPa <= 0){
            hPa = 1015;
            saveHPa(hPa);
        }
    }

    /**
     * Saves the hPa in the config preference.
     * @param hPa double hPa
     */
    private void saveHPa(double hPa){

        if(hPa < 0) throw new IllegalArgumentException("hPa can't be lower than zero!");

        SharedPreferences.Editor config = getSharedPreferences("config", MODE_PRIVATE).edit();
        config.clear();
        config.putString( "hPa", String.valueOf(hPa));
        config.apply();
    }

    /**
     * Gets the hPa from the config preference.
     * @return double hPa, retruns 0.0 if no entry found.
     */
    private double getHPa(){
        SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
        String str = config.getString("hPa", "");
        if(str.isEmpty()){
            return 0.0;
        }

        return Double.valueOf(str);
    }



    /**
     * This method hanldles the button events from the xml methid calls.
     * It increases or decreases the hPa value.
     * @param view
     */
    public void onButtonClick(View view) {

        if (view.getId() == R.id.addButton){

            hPa = hPa + 1;
        }
        else if(view.getId() == R.id.subButton){

            hPa = hPa - 1;
        }

        saveHPa(hPa);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * This method  observes the pressure sensor. Id calculates the height and shows the value
     * in the Textview.
     * @param event
     */
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == mSensorPressure) {

            TextView hpaText = (TextView) findViewById(R.id.pressure);
            hpaText.setText(String.format("%.1f hPa",hPa));

            double height = (288.15/0.0065 )*(1-Math.pow(event.values[0]/hPa,1/5.255));

            TextView meterText = (TextView) findViewById(R.id.meterValue);
            meterText.setText(String.format("%.0f m" , height));
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
