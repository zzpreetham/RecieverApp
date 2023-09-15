package com.royalenfield.recieverapp.activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.EventLogTags;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.progress.progressview.ProgressView;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;
import com.royalenfield.recieverapp.R;
import com.royalenfield.recieverapp.database.DBHandler;
import com.royalenfield.recieverapp.database.LocationDBHandler;
import com.royalenfield.recieverapp.liveDataModel.ABSModel;
import com.royalenfield.recieverapp.liveDataModel.BatterySOHModel;
import com.royalenfield.recieverapp.liveDataModel.ChargingStatusModel;
import com.royalenfield.recieverapp.liveDataModel.ChargingTimeModel;
import com.royalenfield.recieverapp.liveDataModel.DistanceModel;
import com.royalenfield.recieverapp.liveDataModel.HazardTTLModel;
import com.royalenfield.recieverapp.liveDataModel.LeftTTLModel;
import com.royalenfield.recieverapp.liveDataModel.LowSOCModel;
import com.royalenfield.recieverapp.liveDataModel.OdoModel;
import com.royalenfield.recieverapp.liveDataModel.RegenModel;
import com.royalenfield.recieverapp.liveDataModel.ReverseModeModel;
import com.royalenfield.recieverapp.liveDataModel.RidingModeModel;
import com.royalenfield.recieverapp.liveDataModel.RightTTLModel;
import com.royalenfield.recieverapp.liveDataModel.SOCModel;
import com.royalenfield.recieverapp.liveDataModel.SpeedModel;
import com.royalenfield.recieverapp.liveDataModel.VehicleErrorModel;
import com.royalenfield.recieverapp.progressView.LandscapeProgressWidgetCharging;
import com.royalenfield.recieverapp.service.ClusterService;
import com.royalenfield.recieverapp.speedometerView.SpeedoMeterView;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.DoubleUnaryOperator;

import kotlin.jvm.Synchronized;

public class MainActivity extends AppCompatActivity {

    TextView txtspeed;
    TextView txtdistance;
    TextView txtmodes;
    TickerView txtodo;
    SpeedoMeterView speedoMeterView;
    SeekBar seekBar;
    LandscapeProgressWidgetCharging chargingbar;
    LandscapeProgressWidgetCharging chargingbarBattery;

    TextView txtvehiclechrg;
    TextView txtvehiclechrgBattery;
    TextView txtvehiclechrgTime;
    TextView txtvehiclechrgPercent;
    TextView txtchrgtym;
    TextView txtsoc;
    TextView txtLowSoc;
    TextView txtbatterysoh;
    ImageView rightstr;
    ImageView leftstr;
    ImageView hazardstr;
    ImageView errorstr;
    ImageView regenstr;
    ImageView abs_str;
    ImageView vehicle_charge;
    ImageView vehicle_chargeBat;

    public static String LowSoc, right, left, hazard;

    String broker = "tcp://35.200.186.3:1883";
    //String broker = "tcp://0.0.0.0:1883";
    String topic = "gprsData";
    String username = "royalenfield";
    String password = "RoyalEnfieldMqttBroker";
    String clientid = "publish_client";
    String content = "Hello MQTT";

    int qos = 0;

    String packetType = "NR", alertId = "1", packetStatus = "L", date = "29102023", time = "183507", latitude = "12.903077", latitudeDir = "N", longitude = "80.225708", longitudeDir = "E", ignitionStatus = "1";
    String gsmSignalStrength = "31", frameNumber = "8273", obdData, tripId = "1";

    String vehicleErrorIndication = "ON", rideMode = "ES", vehicleCharge = "SEQ_PHASED", regenerationActive = "ON", vehicleRange = "12", vehicleChargingTime = "14",
            reverseMode = "DEPRESSED", stateOfCharge = "1", speedometer = "1", batterySOH = "45", absEvent = "1", vehicleOdometer = "214748364.75";
    public static boolean dataReceived = false;

    public static SpeedModel speedModel;
    public static DistanceModel distanceModel;
    public static OdoModel odoModel;
    public static ChargingStatusModel chargingStatusModel;
    public static ChargingTimeModel chargingTimeModel;
    public static SOCModel socModel;
    public static LowSOCModel lowSOCModel;
    public static BatterySOHModel batterySOHModel;
    public static RightTTLModel rightTTLModel;
    public static LeftTTLModel leftTTLModel;
    public static HazardTTLModel hazardTTLModel;
    public static VehicleErrorModel vehicleErrorModel;
    public static RegenModel regenModel;
    public static ABSModel absModel;
    public static RidingModeModel ridingModeModel;
    public static ReverseModeModel reverseModeModel;
    private ProgressView mProgressView;
    int[] colorList = new int[0];
    private boolean charging = false;

    ConstraintLayout linearLayout_cluster;
    ConstraintLayout linearLayoutbat;
    DBHandler dbHandler;
    private String pattern = "ddMMyyyy";
    private String timeFormate = "HHmmss";
    SimpleDateFormat inputTime = new SimpleDateFormat("hhmmss.sss");
    SimpleDateFormat outputTime = new SimpleDateFormat("hhmmss");

    LocationDBHandler locationDBHandler;
    Cursor locationDataCursor;

    private int stGPSValidity = 0;
    private long lastLocRecivedTimestamp = 0;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 500;

    private UploadData uploadData;
    private LocationManager locationManager;



    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gauge_layout);

        //remove bottom navigation
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        dataReceived = false;
        charging = false;
        stGPSValidity = 0;//0 is Invalid 1 is Valid.
        lastLocRecivedTimestamp = 0;

        dbHandler = new DBHandler(MainActivity.this);
        locationDBHandler = new LocationDBHandler(MainActivity.this);

        locationDataCursor = locationDBHandler.getAllData();
        if(locationDataCursor.getCount() == 0){
            //Log.d("locationCursor","zero");
            //1st time app instalation no DB record existing assiging 0
            latitude = "0";
            longitude = "0";
            stGPSValidity = 0;
        }
        else {
            while (locationDataCursor.moveToNext()) {
                //Log.d("locationCursor", locationDataCursor.getString(0) + "\t" +locationDataCursor.getString(1) + "\t" + locationDataCursor.getString(2));

                latitude = locationDataCursor.getString(1);
                longitude = locationDataCursor.getString(2);
                stGPSValidity = 0;
            }
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        txtspeed = findViewById(R.id.speed);
        txtdistance = findViewById(R.id.rangeTxt);
        txtodo = findViewById(R.id.odoMeter);
        speedoMeterView=findViewById(R.id.speedometerview);
        speedoMeterView.setNeedlecolor(getColor(R.color.white));
        seekBar=findViewById(R.id.seekBar);
        txtmodes=findViewById(R.id.txtmodes);
        txtvehiclechrg=findViewById(R.id.chargePercent);
        txtvehiclechrgBattery=findViewById(R.id.chargePercentbat);
        txtvehiclechrgTime=findViewById(R.id.charging_time);
        txtvehiclechrgPercent=findViewById(R.id.battery_percent);
        rightstr=findViewById(R.id.right_ind);
        leftstr=findViewById(R.id.left_ind);
        hazardstr=findViewById(R.id.vehicle_hazard);
        errorstr=findViewById(R.id.vehicle_error);
        regenstr=findViewById(R.id.vehicle_regen);
        abs_str=findViewById(R.id.vehicle_abs);
        vehicle_charge=findViewById(R.id.vehicle_charge);
        vehicle_chargeBat=findViewById(R.id.imageView8);
        chargingbar=findViewById(R.id.landscapeProgressWidgetCharging);
        chargingbarBattery=findViewById(R.id.landscapeProgressWidgetChargingbat);
        mProgressView = findViewById(R.id.gradiant_progress);
        linearLayout_cluster = findViewById(R.id.linearLayout_cluster);
        linearLayoutbat = findViewById(R.id.linearLayoutbat);

        txtodo.setCharacterLists(TickerUtils.provideNumberList());
        txtodo.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
        Typeface typeface = ResourcesCompat.getFont(MainActivity.this, R.font.audiowide_regular);
        txtodo.setTypeface(typeface);
        txtodo.setGravity(Gravity.CENTER);

        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                //logic to check GPS validity
                if(stGPSValidity == 1){
                    //check difference between current timestamp and location DB timestamp
                    long currenTimeStamp = System.currentTimeMillis();

                    if(currenTimeStamp > lastLocRecivedTimestamp){
                        long timeDif = 0;
                        timeDif = currenTimeStamp - lastLocRecivedTimestamp;
                        if(timeDif > 20000){
                            stGPSValidity = 0;
                        }
                    }
                }
                //date = new SimpleDateFormat(pattern).format(new Date());
                //time = new SimpleDateFormat(timeFormate).format(new Date());
                if(dataReceived) {
                    uploadData = new UploadData();
                    uploadData.execute();
                }
                handler.postDelayed(runnable, delay);
            }
        }, delay);



        startService(new Intent(MainActivity.this, ClusterService.class));

        speedModel= new ViewModelProvider(this).get(SpeedModel.class);
        distanceModel = new ViewModelProvider(this).get(DistanceModel.class);
        odoModel = new ViewModelProvider(this).get(OdoModel.class);
        chargingStatusModel = new ViewModelProvider(this).get(ChargingStatusModel.class);
        chargingTimeModel = new ViewModelProvider(this).get(ChargingTimeModel.class);
        socModel = new ViewModelProvider(this).get(SOCModel.class);
        lowSOCModel = new ViewModelProvider(this).get(LowSOCModel.class);
        batterySOHModel = new ViewModelProvider(this).get(BatterySOHModel.class);
        rightTTLModel = new ViewModelProvider(this).get(RightTTLModel.class);
        leftTTLModel = new ViewModelProvider(this).get(LeftTTLModel.class);
        hazardTTLModel = new ViewModelProvider(this).get(HazardTTLModel.class);
        vehicleErrorModel = new ViewModelProvider(this).get(VehicleErrorModel.class);
        regenModel = new ViewModelProvider(this).get(RegenModel.class);
        absModel = new ViewModelProvider(this).get(ABSModel.class);
        ridingModeModel = new ViewModelProvider(this).get(RidingModeModel.class);
        reverseModeModel = new ViewModelProvider(this).get(ReverseModeModel.class);

        speedModel.getData().observe(this, newData -> {
            // Update UI components with the new data
            speedometer = newData;
            txtspeed.setText(speedometer);
            int speedGauge=Integer.parseInt(speedometer) / 2;
            if (speedGauge>=78){
                speedGauge=78;
            }
            speedoMeterView.setSpeed(speedGauge, true);
            //Log.d("valueee",Integer.parseInt(speedometer) / 2+"");
            //colorList = new int[]{Color.argb((float) 0.1, 0, 0, 0), Color.rgb(218, 170, 0)};
            colorList = new int[]{Color.argb((float) 0.2, 7,7,7), Color.rgb(218, 170, 0)};
            mProgressView.applyGradient(colorList);

            float speedGradent = Float.parseFloat(speedometer)/204;
            if (speedGradent>=0.7647059f){
                speedGradent=0.7647059f;
            }
            mProgressView.setProgress(speedGradent);
        });

        distanceModel.getData().observe(this,newData ->{
            vehicleRange = newData;
            seekBar.setProgress(Integer.parseInt(vehicleRange));
            txtdistance.setText(vehicleRange + " km");
        });

        odoModel.getData().observe(this,newData->{
            vehicleOdometer = newData;
            //vehicleOdometer = String.valueOf(Integer.parseInt(vehicleOdometer)* 234);
            txtodo.setText(vehicleOdometer);
           // txtodo.setText("2500000");
        });

        chargingStatusModel.getData().observe(this,newData->{
            vehicleCharge = newData;
            vehicle_charge.setVisibility(View.VISIBLE);
            if (vehicleCharge.equalsIgnoreCase("DISABLED")) {
                Glide.with(getApplicationContext()).load(R.drawable.charge).into(vehicle_charge);
                charging = false;
                linearLayout_cluster.setVisibility(View.VISIBLE);
                linearLayoutbat.setVisibility(View.GONE);
                speedoMeterView.setVisibility(View.VISIBLE);
            } else if (vehicleCharge.equalsIgnoreCase("STATIC")) {
                Glide.with(getApplicationContext()).load(R.drawable.charge_error).into(vehicle_charge);
                charging = false;
            } else if (vehicleCharge.equalsIgnoreCase("FREQ")) {
                Glide.with(getApplicationContext()).load(R.drawable.charge_on).into(vehicle_charge);
                Glide.with(getApplicationContext()).load(R.drawable.charge_on).into(vehicle_chargeBat);
                charging = true;
                linearLayout_cluster.setVisibility(View.GONE);
                linearLayoutbat.setVisibility(View.VISIBLE);
                speedoMeterView.setVisibility(View.GONE);
            } else if (vehicleCharge.equalsIgnoreCase("OUTPUT")) {
                Glide.with(getApplicationContext()).load(R.drawable.charge_complete).into(vehicle_charge);
                Glide.with(getApplicationContext()).load(R.drawable.charge_complete).into(vehicle_chargeBat);
                charging = true;
                linearLayout_cluster.setVisibility(View.GONE);
                linearLayoutbat.setVisibility(View.VISIBLE);
                speedoMeterView.setVisibility(View.GONE);
            }
        });

        chargingTimeModel.getData().observe(this,newData->{
            vehicleChargingTime = newData;
            txtvehiclechrgTime.setText(getTotalTime(Integer.parseInt(vehicleChargingTime)));
        });

        socModel.getData().observe(this,newData->{
            stateOfCharge = newData;
            if (stateOfCharge.equalsIgnoreCase("0")){
            }
            else {
                if(!charging) {
                    txtvehiclechrg.setText(stateOfCharge + "%");
                    chargingbar.setPercentage(Integer.parseInt(stateOfCharge));
                }
                else{
                    txtvehiclechrgBattery.setText(stateOfCharge + "%");
                    chargingbarBattery.setPercentage(Integer.parseInt(stateOfCharge));
                }
            }
        });

        lowSOCModel.getData().observe(this,newData->{
            LowSoc = newData;
        });

        batterySOHModel.getData().observe(this,newData->{
            batterySOH = newData;
            txtvehiclechrgPercent.setText(batterySOH + "%");
        });
        rightTTLModel.getData().observe(this,newData->{
            right = newData;
            if (right.equalsIgnoreCase("true")){
                Glide.with(getApplicationContext()).load(R.drawable.right_on).into(rightstr);
            } else if (right.equalsIgnoreCase("false")) {
                Glide.with(getApplicationContext()).load(R.drawable.right).into(rightstr);
            }
        });
        leftTTLModel.getData().observe(this,newData->{
            left = newData;
            if (left.equalsIgnoreCase("true")){
                Glide.with(getApplicationContext()).load(R.drawable.left_on).into(leftstr);
            } else if (left.equalsIgnoreCase("false")) {
                Glide.with(getApplicationContext()).load(R.drawable.left).into(leftstr);
            }
        });
        hazardTTLModel.getData().observe(this,newData->{
            hazard = newData;
            if (hazard.equalsIgnoreCase("true")){
                Glide.with(getApplicationContext()).load(R.drawable.hazard_on).into(hazardstr);
            } else if (hazard.equalsIgnoreCase("false")) {
                Glide.with(getApplicationContext()).load(R.drawable.hazard).into(hazardstr);
            }
        });
        vehicleErrorModel.getData().observe(this,newData->{
            vehicleErrorIndication = newData;
            if (vehicleErrorIndication.equalsIgnoreCase("true")){
                vehicleErrorIndication = "ON";
                Glide.with(getApplicationContext()).load(R.drawable.dtc_on).into(errorstr);
            } else if (vehicleErrorIndication.equalsIgnoreCase("false")) {
                vehicleErrorIndication = "OFF";
                Glide.with(getApplicationContext()).load(R.drawable.dtc).into(errorstr);
            }
        });

        regenModel.getData().observe(this,newData->{
            regenerationActive = newData;
            if (regenerationActive.equalsIgnoreCase("true")){
                regenerationActive = "ON";
                Glide.with(getApplicationContext()).load(R.drawable.power_on).into(regenstr);
            } else if (regenerationActive.equalsIgnoreCase("false")) {
                regenerationActive = "OFF";
                Glide.with(getApplicationContext()).load(R.drawable.power).into(regenstr);
            }
        });

        absModel.getData().observe(this,newData->{
            absEvent = newData;
            if (absEvent.equalsIgnoreCase("0")){
                Glide.with(getApplicationContext()).load(R.drawable.abs_on).into(abs_str);
            } else if (absEvent.equalsIgnoreCase("1")) {
                Glide.with(getApplicationContext()).load(R.drawable.abs).into(abs_str);
            }
        });
        ridingModeModel.getData().observe(this,newData->{
            rideMode = newData;
            if (rideMode.equalsIgnoreCase("ES")) {
                rideMode = "ES";
                txtmodes.setText("Eco");

            } else if (rideMode.equalsIgnoreCase("ST")) {
                rideMode = "ST";
                txtmodes.setText("Tour");

            } else if (rideMode.equalsIgnoreCase("IS")) {
                rideMode = "IS";
                txtmodes.setText("Sports");

            }
        });
        reverseModeModel.getData().observe(this,newData->{
           String revMode = newData;
            if(revMode.equalsIgnoreCase("true")||revMode.equalsIgnoreCase("ON")){
                reverseMode = "ON";
            }
            else{
                reverseMode = "OFF";
            }
        });


    }

    public static String getTotalTime(int time) {
        int hour = time / 3600;
        int minu = (time % 3600) / 60;
        int sec = time % 60;
        String str = "";
        if (hour == 0) {
            str = String.format("%02d:%02d", minu, sec);
        } else {/*  w ww  .j  av  a2s  .c om*/
            str = String.format("%02d:%02d:%02d", hour, minu, sec);
        }
        return str;
    }

    public class startMqttTimer extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            return null;
        }
    }

    public class UploadData extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... voids) {
           /* obdData="ON|SEQ_PHASED|PHASED|"+ vehicleErrorIndication +"|[1126;0;0;0;0;0]|"+ rideMode +"|1095|29.000|"+ vehicleCharge +"|"+ regenerationActive +"|2.334|0|0|19.938|36|102.719|86.938|"+ vehicleRange +
                    "|312|0.513|0.160|0.160|0.547|1.000|0.000|[41;0;0;0;0;0]|[0.000;0;0;0;0;0]|12.930|5.676|"+ vehicleChargingTime +"|0.505|6.383|2.364|6.562|-1.000|0.000|36.719|0.938|2.404|"
                    + reverseMode +"|0.000|"+ stateOfCharge +"|"+ speedometer +"|-3.100|12420.60|"+ batterySOH +"|8.00|12.95|[0;0;0;0;0;0;0;0;0]|0.00|1.00|"+absEvent+"|1.00|0.00|0.00|0.00|0.00|0.00|0.00|0.00|0.00|0.00|"+ vehicleOdometer;
            */

            obdData="ON|SEQ_PHASED|PHASED|"+ vehicleErrorIndication +"|[1126;0;0;0;0;0]|"+ rideMode +"|1095|29.000|"+ vehicleCharge +"|"+ regenerationActive +"|2.334|0|0|19.938|36|102.719|86.938|"+ vehicleRange +
                    "|312|0.513|0.160|0.160|0.547|1.000|0.000|[41;0;0;0;0;0]|[0.000;0;0;0;0;0]|12.930|5.676|"+ vehicleChargingTime +"|0.505|6.383|2.364|6.562|-1.000|0.000|36.719|0.938|2.404|"
                    + reverseMode +"|0.000|"+ stateOfCharge +"|"+ speedometer +"|-3.100|12420.60|"+ batterySOH +"|8.00|12.95|[0;0;0;0;0;0;0;0;0]|0.00|1.00|0.00|1.00|0.00|0.00|0.00|0.00";

            content = "{\"payload\":\"$,RE-CONNECT,506.6,4.4,V9.4,"+packetType+","+alertId+","+packetStatus+",660906045499651,"+stGPSValidity+"," +
                    ""+date+","+time+","+latitude+","+latitudeDir+","+longitude+","+longitudeDir+",0.0,0,0,0,0.0,0.0,airtel,"+ignitionStatus+",12.31,"+gsmSignalStrength+
                    ",1,"+frameNumber+",0,"+obdData+",,P0030,ME3U5S5F1JA125717,"+tripId+",M4A,*4e\"}";

            Log.d("string_val",content);

            /*ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
            if (netInfo == null){

            }else{
                save_mqtt_records(content,"1",String.valueOf(System.currentTimeMillis()));
            }*/

            return content;
        }

        @Override
        protected void onPostExecute(String contentVal) {
            super.onPostExecute(contentVal);
            try {
                ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
                if (netInfo == null){
                    save_mqtt_records(content,"0",String.valueOf(System.currentTimeMillis()));
                }else{
                    try {
                        save_mqtt_records(content,"1",String.valueOf(System.currentTimeMillis()));
                        MqttClient client = new MqttClient(broker, clientid, new MemoryPersistence());
                        MqttConnectOptions options = new MqttConnectOptions();
                        options.setUserName(username);
                        options.setPassword(password.toCharArray());
                        options.setConnectionTimeout(60);
                        options.setKeepAliveInterval(300);
                        // connect
                        Log.d("mqtt","Connected"+System.currentTimeMillis());
                        client.connect(options);
                        // create message and setup QoS
                        MqttMessage message = new MqttMessage(content.getBytes());
                        message.setQos(qos);
                        // publish message
                        client.publish(topic, message);
                        System.out.println("Message published");
                        System.out.println("topic: " + topic);
                        System.out.println("message content: " + content);
                        // disconnect
                        Log.d("mqtt","Disconnected");
                        client.disconnect();
                        // close client
                        client.close();
                    }
                    catch (MqttException e){
                        save_mqtt_records(content,"0",String.valueOf(System.currentTimeMillis()));
                        e.printStackTrace();
                    }
                }
                uploadData.cancel(true);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void save_mqtt_records(String rawData,String uploadStatus,String timestamp){
        dbHandler.addNewCourse(rawData, uploadStatus, timestamp);
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Handle the new location here
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            stGPSValidity = 1;
            lastLocRecivedTimestamp = System.currentTimeMillis();


            /*tvLatitude.setText(latitude+"");
            tvLongitude.setText(longitude+"");*/

            if(locationDataCursor.getCount() == 0) {

                //Log.d("locationc",latitude+"\t"+longitude);
                locationDBHandler.addNewLocation(latitude,longitude,String.valueOf(lastLocRecivedTimestamp));
                locationDataCursor = locationDBHandler.getAllData();
            }
            else{
                //Log.d("locationu",latitude+"\t"+longitude);
                locationDBHandler.updateLocation(latitude,longitude,String.valueOf(lastLocRecivedTimestamp));
            }
            // ...
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Location provider status changed (e.g., GPS signal lost)
        }

        public void onProviderEnabled(String provider) {
            // Location provider enabled (e.g., GPS turned on)
        }

        public void onProviderDisabled(String provider) {
            // Location provider disabled (e.g., GPS turned off)
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Use GPS_PROVIDER for high-accuracy location
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,  // Minimum time interval between updates (in milliseconds)
                    0,  // Minimum distance between updates (in meters)
                    locationListener
            );
        } else {
            // If GPS_PROVIDER is not available, fall back to NETWORK_PROVIDER
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    0,
                    locationListener
            );
        }

        locationManager.requestSingleUpdate(criteria, locationListener, null);
        locationManager.addNmeaListener(new OnNmeaMessageListener() {
            @Override
            public void onNmeaMessage(String s, long l) {
                try {
                    //Log.d(TAG,"Nmea Received :");
                    Log.d(TAG,"Timestamp is :" +l+"   nmea is :"+s);

                    //$GNRMC,075030.00,A,1254.161924,N,08013.621093,E,0.0,,150923,1.1,W,A,V*6E
                    if (s.contains("$GNGGA")) {
                        String[] gpsNmea = s.split(",");
                        //Log.d("lat",gpsNmea[2]+"\t"+gpsNmea[3]+"\n"+ gpsNmea[4]+"\t"+gpsNmea[5]+"\n");
                        if(!gpsNmea[3].equalsIgnoreCase("")) {
                            latitudeDir = gpsNmea[3];
                        }
                        if(!gpsNmea[5].equalsIgnoreCase("")) {
                            longitudeDir = gpsNmea[5];
                        }

                    }
                    if (s.contains("$GNRMC")) {
                        String[] gpsNmea = s.split(",");
                        //Log.d("valuesss",gpsNmea[1]+"\t"+gpsNmea[9]);
                        if(!gpsNmea[9].equalsIgnoreCase("")) {
                            date = gpsNmea[9];
                        }
                        try {
                            if(!gpsNmea[1].equalsIgnoreCase("")) {
                                time = outputTime.format(inputTime.parse(gpsNmea[1]));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

    }
}