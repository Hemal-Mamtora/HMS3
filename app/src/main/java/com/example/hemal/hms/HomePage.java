package com.example.hemal.hms;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.support.v4.app.NotificationCompat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import java.lang.*;


public class HomePage extends AppCompatActivity implements SensorEventListener, StepListener {
    Context context;
    private TextView textView;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    DBHelper myDB = new DBHelper(this);
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private static final String TEXT_DISTANCE = "total distance (in km):";
    private static final String TEXT_SPEED = "Speed(in kmph):";

    private static final String TEMP_TEXT_SPEED = " temp Speed(in kmph):";
    private static final String TEMP_TEXT_TIME = " temp time :";
    private static final String TEMP_TEXT_DIST = "temp dist (in km):";

    private int numSteps;
    private float distance, d, speed, s;
    private TextView TvSteps, TvDistance, TvSpeed, TvCalories;
    private TextView TempTvspeed, TempTvDistance, TempTvintervalOnPause;
    private Button BtnStart, BtnStop, BtnPause;
    private Chronometer sChronometer;
    private long sLastStopTime = 0;
    //private long tspeed,tdistance,tintervalOnPause;
    //String Ntime[];
    Button logout_btn;
    private float Hour = 3600;
    private float Min = 60;
    private float Second = 1;
    private int Day = 3600 * 24;
    private float TimeInSeconds = 0;
    private long flag;
    private CharSequence cs;
    private String sex = "male";
    HomePage instance = this;
    Button bmi_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        logout_btn = (Button) findViewById(R.id.logout_btn);
        bmi_btn = (Button) findViewById(R.id.bmi_btn);

        //myDB.setCalories(1,100,1000);

        //get an instance of location manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                location.getLatitude();
                Toast.makeText(context, "Current speed:" + location.getSpeed(), Toast.LENGTH_SHORT).show();

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        //not working locationManager.requestLocationUpdates(LocationManager.GPS_PROV356cv  IDER, 0, 0, locationListener);




    // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);


        TvSpeed = (TextView) findViewById(R.id.tv_speed);
        TvDistance = (TextView) findViewById(R.id.tv_distance);
        TvSteps = (TextView) findViewById(R.id.tv_steps);
        BtnStart = (Button) findViewById(R.id.btn_start);
        BtnPause = (Button) findViewById(R.id.btn_pause);
        BtnStop = (Button) findViewById(R.id.btn_stop);
        sChronometer = (Chronometer) findViewById(R.id.chronometer2);
        TvCalories = (TextView) findViewById(R.id.textView);


        BtnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // numSteps = 0;
                sensorManager.registerListener(HomePage.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
                chronoStart();


            }
        });


        BtnPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                sensorManager.unregisterListener(HomePage.this);
                chronoPause();


            }
        });
        BtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                numSteps = 0;
                sensorManager.registerListener(HomePage.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
                sLastStopTime = 0;
                chronoStart();

            }

        });
        logout();
        Bmi();




    }

    public void logout() {

        logout_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this, LoginActivity.class));
            }
        });
    }

    public void Bmi() {

        bmi_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this, BMI.class));
            }
        });
    }


    //CHRONOMETER
    public void chronoStart() {

        if (sLastStopTime == 0) {
            sChronometer.setBase(SystemClock.elapsedRealtime());
        } else {
            long intervalOnPause = (SystemClock.elapsedRealtime() - sLastStopTime);

            // TempTvintervalOnPause.setText(TEMP_TEXT_TIME+" --elapsed Time--"+intervalOnPause+"---slaststoptime-"+sLastStopTime+"---" +
            //    "--getcd++"+sChronometer.getContentDescription());


            sChronometer.setBase(sChronometer.getBase() + intervalOnPause);
            sChronometer.getBase();
        }
        sChronometer.start();
    }

    public void chronoPause() {
        sChronometer.stop();
        sLastStopTime = SystemClock.elapsedRealtime();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        //calorie_count()
        //estimated_calorie_burn()
        //make_db_calls
        double calburnt;
        String calburnt1;
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);
        d = getDistance(numSteps);


        s = getSpeed(distance);
        TvSpeed.setText(TEXT_SPEED + s + "kmph");
        TvDistance.setText(TEXT_DISTANCE + d + "---Time--- " + TimeInSeconds);
        float weight = myDB.wtInPound();
        calburnt = (weight * 0.57 / 1.67)*d;
        calburnt1 = new Double(calburnt).toString();
        TvCalories.setText("Calories Burnt : "+ calburnt1 +" kcal");
    }

    //CONVERTING TIME FROM CHRONOMETER TO SECONDS TO CALCULATE SPEED
    public float conTime() {
        String tempString;
        String contentDscString;
        cs = sChronometer.getContentDescription();
        contentDscString = cs.toString();
        String tempDisplayTime[];
        tempDisplayTime = contentDscString.split(" ");
       // Toast.makeText(instance, "time: " + tempDisplayTime[1], Toast.LENGTH_SHORT).show();
        for (int i = 0; i < tempDisplayTime.length; i = i + 2) {
            float tempNo = Float.parseFloat(tempDisplayTime[i]);
            tempString = tempDisplayTime[i + 1];
            if (tempDisplayTime[i + 1].contains("hour")) {
                if (tempNo !=0)
                TimeInSeconds += (tempNo * Hour)-TimeInSeconds;
            }
            if (tempDisplayTime[i + 1].contains("minute")) {
                if (tempNo !=0)
                TimeInSeconds += (tempNo * Min)-TimeInSeconds;
            }
            if (tempDisplayTime[i + 1].contains("seconds")) {
                if (tempNo !=0)
                TimeInSeconds += (tempNo * Second)-TimeInSeconds;
            }

        }
        ;
        return TimeInSeconds;

    }

    public float getDistance(int numSteps) {
        if (sex.contains("male")) {
            distance = (float) (numSteps * 78) / 100000;
        } else {
            distance = (float) (numSteps * 74) / 100000;
        }
        return distance;

    }

    public float getSpeed(float distance) {
        //TempTvintervalOnPause.setText(TEMP_TEXT_TIME+sChronometer.getContentDescription());
        CharSequence cs = sChronometer.getContentDescription();
        // TempTvintervalOnPause.getText();
        //int ntime=Integer.parseInt(cs.toString());

        speed = ((distance) / (conTime())) * 3600;
        // TempTvDistance.setText(TEMP_TEXT_SPEED+distance);
        return speed;



    }




}