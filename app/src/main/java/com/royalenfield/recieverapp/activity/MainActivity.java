package com.royalenfield.recieverapp.activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.progress.progressview.ProgressView;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;
import com.royalenfield.recieverapp.R;
import com.royalenfield.recieverapp.database.MqttDBHelper;
import com.royalenfield.recieverapp.database.LocationDBHandler;
import com.royalenfield.recieverapp.database.StatisticsDBHandler;
import com.royalenfield.recieverapp.liveDataModel.ABSModel;
import com.royalenfield.recieverapp.liveDataModel.BatterySOHModel;
import com.royalenfield.recieverapp.liveDataModel.ChargingStatusModel;
import com.royalenfield.recieverapp.liveDataModel.ChargingTimeModel;
import com.royalenfield.recieverapp.liveDataModel.DataReceiveModel;
import com.royalenfield.recieverapp.liveDataModel.DistanceModel;
import com.royalenfield.recieverapp.liveDataModel.HazardTTLModel;
import com.royalenfield.recieverapp.liveDataModel.IgnitionModel;
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
import com.royalenfield.recieverapp.model.MqttDataModel;
import com.royalenfield.recieverapp.progressView.LandscapeProgressWidgetCharging;
import com.royalenfield.recieverapp.service.ClusterService;
import com.royalenfield.recieverapp.speedometerView.SpeedoMeterView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import kotlin.jvm.Synchronized;
import soup.neumorphism.NeumorphCardView;

public class MainActivity extends AppCompatActivity implements MqttCallbackExtended{

    TextView txtspeed;
    TextView txtdistance;
    TextView txtmodes;
    TickerView txtodo;
    SpeedoMeterView speedoMeterView;
    SeekBar seekBar;
    LandscapeProgressWidgetCharging chargingbar;
    LandscapeProgressWidgetCharging chargingbarBattery;
    int odo_pad_width = 6;

    TextView txtvehiclechrg;
    TextView txtvehiclechrgBattery;
    TextView txtvehiclechrgTime;
    TextView txtvehiclechrgPercent;
    ImageView rightstr;
    ImageView leftstr;
    ImageView hazardstr;
    ImageView errorstr;
    ImageView regenstr;
    ImageView abs_str;
    ImageView vehicle_charge;
    ImageView vehicle_soc;
    ImageView vehicle_chargeBat;

    ImageView mqttUploadStatusImg;

    public static String LowSocThreshold = "20", right, left, hazard;

    //MQTT server Creditentials
    String broker = "tcp://35.200.186.3:1883";
    String username = "royalenfield";
    String password = "RoyalEnfieldMqttBroker";
    String clientid = "publish_client";
    String content = "";

    String currentPacketContent = "hello MQTT";

    int qos = 0;
    String networkDate = "";
    String networkTime = "";

    String packetType = "NR", alertId = "1", packetStatus = "L", date = "29102023", time = "183507", latitude = "12.903077", latitudeDir = "N", longitude = "80.225708", longitudeDir = "E", ignitionStatus = "1";
    String gsmSignalStrength = "31", frameNumber = "0", obdData, tripId = "1";

    String vehicleErrorIndication = "OFF", rideMode = "ES", vehicleCharge = "DISABLED", regenerationActive = "OFF", vehicleRange = "12", vehicleChargingTime = "14",
            reverseMode = "DEPRESSED", currStateOfCharge = "1", speedometer = "1", batterySOH = "45", absEvent = "1", vehicleOdometer = "214748364.75";
    public static boolean dataReceived = false;

    private String ignitionStr = "ON";
    private String rideModeStr = "ECO";
    private String vehicleChargeMode = "DISCONNECTED";
    private String odoValue = "0";
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
    public static IgnitionModel ignitionModel;
    public static DataReceiveModel dataReceiveModel;
    private Animation animation1;
    private ProgressView mProgressView;
    int[] colorList = new int[0];
    private boolean charging = false;

    ConstraintLayout linearLayout_cluster;
    ConstraintLayout linearLayoutbat;
    MqttDBHelper mqttDbHandler;
    private String pattern = "ddMMyyyy";
    private String timeFormate = "HHmmss";
    SimpleDateFormat inputTime = new SimpleDateFormat("hhmmss.sss");
    SimpleDateFormat outputTime = new SimpleDateFormat("hhmmss");

    SimpleDateFormat inputDate = new SimpleDateFormat("ddMMyy");
    SimpleDateFormat outputDate = new SimpleDateFormat("ddMMyyyy");


    LocationDBHandler locationDBHandler;
    StatisticsDBHandler statisticsDBHandler;
    Cursor locationDataCursor;
    Cursor statisticsDataCursor;

    private int stGPSValidity = 0;
    private long lastLocRecivedTimestamp = 0;
    private long lastODOTimestamp = 0;
    Handler handler = new Handler();
    Runnable runnable;
    int mqttServerUploadDelay = 1000;
    //int mqttServerAsyncTaskDelay = mqttServerUploadDelay / 5;
    int mqttServerAsyncTaskDelay = 400;

    private LocationManager locationManager;

    //private boolean dataReceived = true;

    private boolean leftBlinking = false;
    private boolean rightBlinking = false;
    private boolean hazardBlinking = false;

    private long mPrevTimeMs = 0;
    private double totalDistance = 0;
    private int maxRangeVal = 0;
    NeumorphCardView neumorphCardView;

    public boolean currMqttPacketUpload = false;

    private MqttClient client;

    private boolean mqttConnect = false;
    MqttConnectOptions options;
    MqttConnect mqttConnectTask;



    @SuppressLint({"SimpleDateFormat", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Full screen mode enabling
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gauge_layout);

        //remove bottom navigation
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        dataReceived = false;
        charging = false;
        currMqttPacketUpload = false;
        stGPSValidity = 0;//0 is Invalid 1 is Valid.
        lastLocRecivedTimestamp = 0;
        LowSocThreshold = "20";
        maxRangeVal = 0;
        mqttConnect = false;
        init();

        //Initilize max range
        calculateRangeUsingSOC("1","ECO");

        txtodo.setCharacterLists(TickerUtils.provideNumberList());
        txtodo.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
        Typeface typeface = ResourcesCompat.getFont(MainActivity.this, R.font.audiowide_regular);
        txtodo.setTypeface(typeface);
        txtodo.setGravity(Gravity.CENTER);


        locationDataCursor = locationDBHandler.getAllData();
        if(locationDataCursor.getCount() == 0){
            //Log.d("locationCursor","zero");
            //1st time app instalation no DB record existing assiging 0
            latitude = "0";
            longitude = "0";
            stGPSValidity = 0;
        }
        else {
            //Getting last location from SQLlite DB
            while (locationDataCursor.moveToNext()) {
                //Log.d("locationCursor", locationDataCursor.getString(0) + "\t" +locationDataCursor.getString(1) + "\t" + locationDataCursor.getString(2));

                latitude = locationDataCursor.getString(1);
                longitude = locationDataCursor.getString(2);
                stGPSValidity = 0;
            }
        }

        statisticsDataCursor = statisticsDBHandler.getAllData();
        Log.d("count",statisticsDataCursor.getCount()+"\t");
        if(statisticsDataCursor.getCount() == 0){
            odoValue = "0";
            txtodo.setTextColor(getResources().getColor(R.color.textColorDark));
            String padded = String.format("%0"+odo_pad_width+"d", Integer.parseInt(odoValue));
            txtodo.setText(padded);
        }
        else{
            while (statisticsDataCursor.moveToNext()){
                totalDistance = Double.parseDouble(statisticsDataCursor.getString(1));
                Log.d("dist",totalDistance+"\t");
                long totDist = Math.round(totalDistance);
                odoValue = String.valueOf(totDist);
                String padded = String.format("%0"+odo_pad_width+"d", Integer.parseInt(odoValue));
                txtodo.setText(padded);
            }
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Running timer for MQTT data upload to server with delay of 2sec interval
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                Log.d("mainRunCurPck",System.currentTimeMillis()+"");
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
                    try {
                        Log.d("mainRunCurPck1",System.currentTimeMillis()+"");
                        //calling MQTT creation function
                        constructCurrMqttPacket();
                    }
                    catch (Exception e){
                        //TODO:packet missed to be handled later.
                        e.printStackTrace();
                    }

                }
                handler.postDelayed(runnable, mqttServerUploadDelay);
            }
        }, mqttServerUploadDelay);


        //Starting the receiver service
        startService(new Intent(MainActivity.this, ClusterService.class));

        //Receiving Speed data from MutableLiveData
        speedModel.getData().observe(this, newData -> {
            // Update UI components with the new data
            speedometer = newData;
            if(Integer.parseInt(speedometer) < 180) {
                txtspeed.setText(speedometer);
                int speedGauge = Integer.parseInt(speedometer) / 2;
                if (speedGauge >= 78) {
                    speedGauge = 78;
                }
                speedoMeterView.setSpeed(speedGauge, true);
                colorList = new int[]{Color.argb((float) 0.2, 7, 7, 7), Color.rgb(218, 170, 0)};

                float speedGradent = Float.parseFloat(speedometer) / 204;
                if (speedGradent >= 0.7647059f) {
                    speedGradent = 0.7647059f;
                }
                mProgressView.applyGradient(colorList);
                mProgressView.setProgress(speedGradent);
                calulateODODistance(Integer.parseInt(speedometer));
            }
        });

        distanceModel.getData().observe(this,newData ->{
            // Update UI components with the new data
            vehicleRange = newData;
                int vehRangeProg = 0;
                vehRangeProg = ((Integer.parseInt(vehicleRange) * 100)/maxRangeVal);
                String curVehPer = String.valueOf(Math.round(vehRangeProg));
                seekBar.setProgress(Integer.parseInt(curVehPer));
                txtdistance.setTextColor(getResources().getColor(R.color.white));
                txtdistance.setText(vehicleRange + " km");
        });

        odoModel.getData().observe(this,newData->{
            // Update UI components with the new data
            vehicleOdometer = newData;
            //vehicleOdometer = String.valueOf(Integer.parseInt(vehicleOdometer)* 234);
            //String padded = String.format("%0"+odo_pad_width+"d", vehicleOdometer);
            String padded = String.format("%0"+odo_pad_width+"d", Integer.parseInt(vehicleOdometer));
            txtodo.setText(padded);
           // txtodo.setText("2500000");
        });

        chargingStatusModel.getData().observe(this,newData->{
            // Update UI components with the new data
            vehicleChargeMode = newData;
            vehicle_charge.setVisibility(View.VISIBLE);
            if (vehicleChargeMode.equalsIgnoreCase("DISCONNECTED")) {
                vehicleCharge = "DISABLED";
                vehicle_charge.setBackground(getDrawable(R.drawable.charge));
                //Glide.with(getApplicationContext()).load(R.drawable.charge).into(vehicle_charge);
                charging = false;
                linearLayout_cluster.setVisibility(View.VISIBLE);
                linearLayoutbat.setVisibility(View.GONE);
                speedoMeterView.setVisibility(View.VISIBLE);
            } else if (vehicleChargeMode.equalsIgnoreCase("FAULT")) {
                vehicleCharge = "STATIC";
                Glide.with(getApplicationContext()).load(R.drawable.charge_error).into(vehicle_charge);
                charging = false;
            } else if (vehicleChargeMode.equalsIgnoreCase("CONNECTED")) {
                vehicleCharge = "FREQ";
                Glide.with(getApplicationContext()).load(R.drawable.charge_on).into(vehicle_charge);
                Glide.with(getApplicationContext()).load(R.drawable.charge_on).into(vehicle_chargeBat);
                charging = true;
                linearLayout_cluster.setVisibility(View.GONE);
                linearLayoutbat.setVisibility(View.VISIBLE);
                speedoMeterView.setVisibility(View.GONE);
            } else if (vehicleChargeMode.equalsIgnoreCase("COMPLETED")) {
                vehicleCharge = "OUTPUT";
                Glide.with(getApplicationContext()).load(R.drawable.charge_complete).into(vehicle_charge);
                Glide.with(getApplicationContext()).load(R.drawable.charge_complete).into(vehicle_chargeBat);
                charging = true;
                linearLayout_cluster.setVisibility(View.GONE);
                linearLayoutbat.setVisibility(View.VISIBLE);
                speedoMeterView.setVisibility(View.GONE);
            }
        });

        chargingTimeModel.getData().observe(this,newData->{
            // Update UI components with the new data
            vehicleChargingTime = newData;
            txtvehiclechrgTime.setText(getTotalTime(Integer.parseInt(vehicleChargingTime)));
        });

        socModel.getData().observe(this,newData->{
            // Update UI components with the new data
            currStateOfCharge = newData;
            if (currStateOfCharge.equalsIgnoreCase("0")){
            }
            else {
                if(!charging) {
                    txtvehiclechrg.setText(currStateOfCharge + "%");
                    chargingbar.setPadding(0,0,20,0);
                    chargingbar.setPercentage(Integer.parseInt(currStateOfCharge));
                    /***********************************/
                    /**logic to calculate Range**/
                    //range is calculated based on ride mode. Simple factor to ridemode is given
                    String currRange = calculateRangeUsingSOC(currStateOfCharge,rideModeStr);
                    int vehRangeProg = 0;
                    vehRangeProg = ((Integer.parseInt(currRange) * 100) / maxRangeVal);
                    String curVehPer = String.valueOf(Math.round(vehRangeProg));
                    seekBar.setProgress(Integer.parseInt(curVehPer));
                    //seekBar.setProgress(Integer.parseInt(currRange));
                    txtdistance.setText(currRange + " km");

                    calculateLowSOC(LowSocThreshold, currStateOfCharge);
                }
                else{
                    txtvehiclechrgBattery.setText(currStateOfCharge + "%");
                    chargingbarBattery.setPercentage(Integer.parseInt(currStateOfCharge));

                }
            }
        });

        lowSOCModel.getData().observe(this,newData->{
            // Update UI components with the new data
            LowSocThreshold = newData;
            calculateLowSOC(LowSocThreshold, currStateOfCharge);
        });

        batterySOHModel.getData().observe(this,newData->{
            // Update UI components with the new data
            batterySOH = newData;
            txtvehiclechrgPercent.setText(batterySOH + "%");
        });
        rightTTLModel.getData().observe(this,newData->{
            // Update UI components with the new data
            right = newData;
            if (right.equalsIgnoreCase("true")){
                if(!rightBlinking){
                    rightBlinking = true;
                    rightstr.startAnimation(animation1);
                }
                Glide.with(getApplicationContext()).load(R.drawable.right_on).into(rightstr);
            } else if (right.equalsIgnoreCase("false")) {
                if(rightBlinking){
                    rightBlinking = false;
                    rightstr.clearAnimation();
                }
                Glide.with(getApplicationContext()).load(R.drawable.right).into(rightstr);
            }
        });
        leftTTLModel.getData().observe(this,newData->{
            // Update UI components with the new data
            left = newData;
            if (left.equalsIgnoreCase("true")){
                if(!leftBlinking){
                    leftBlinking = true;
                    leftstr.startAnimation(animation1);
                }
                Glide.with(getApplicationContext()).load(R.drawable.left_on).into(leftstr);
            } else if (left.equalsIgnoreCase("false")) {
                if(leftBlinking){
                    leftBlinking = false;
                    leftstr.clearAnimation();
                }
                Glide.with(getApplicationContext()).load(R.drawable.left).into(leftstr);
            }
        });
        hazardTTLModel.getData().observe(this,newData->{
            // Update UI components with the new data
            hazard = newData;
            if (hazard.equalsIgnoreCase("true")){
                if(!hazardBlinking){
                    hazardBlinking = true;
                    hazardstr.startAnimation(animation1);
                }
                Glide.with(getApplicationContext()).load(R.drawable.hazard_on).into(hazardstr);
            } else if (hazard.equalsIgnoreCase("false")) {
                if(hazardBlinking){
                    hazardBlinking = false;
                    hazardstr.clearAnimation();
                }
                Glide.with(getApplicationContext()).load(R.drawable.hazard).into(hazardstr);
            }
        });
        vehicleErrorModel.getData().observe(this,newData->{
            // Update UI components with the new data
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
            // Update UI components with the new data
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
            // Update UI components with the new data
            absEvent = newData;
            if (absEvent.equalsIgnoreCase("0")){
                Glide.with(getApplicationContext()).load(R.drawable.abs_on).into(abs_str);
            } else if (absEvent.equalsIgnoreCase("1")) {
                Glide.with(getApplicationContext()).load(R.drawable.abs).into(abs_str);
            }
        });
        ridingModeModel.getData().observe(this,newData->{
            // Update UI components with the new data
            rideModeStr = newData;
            if (rideModeStr.equalsIgnoreCase("ECO")) {
                rideMode = "ES";
                txtmodes.setText("Eco");
                neumorphCardView.setShadowColorDark(getResources().getColor(R.color.ecoColor));
                neumorphCardView.setShadowColorLight(getResources().getColor(R.color.ecoColor));
                //neumorphCardView.setStrokeColor(getColor(R.color.ecoColor));

            } else if (rideModeStr.equalsIgnoreCase("TOUR")) {
                rideMode = "ST";
                txtmodes.setText("Tour");
                neumorphCardView.setShadowColorDark(getResources().getColor(R.color.tourColor));
                neumorphCardView.setShadowColorLight(getResources().getColor(R.color.tourColor));
                //neumorphCardView.setStrokeColor(ColorStateList.valueOf(R.color.tourColor));

            } else if (rideModeStr.equalsIgnoreCase("SPORT")) {
                rideMode = "IS";
                txtmodes.setText("Sport");
                neumorphCardView.setShadowColorDark(getResources().getColor(R.color.sportColor));
                neumorphCardView.setShadowColorLight(getResources().getColor(R.color.sportColor));
                //neumorphCardView.setStrokeColor(ge);

            }
            /***********************************/
            /**logic to calculate Range**/
            //range is calculated based on ride mode. Simple factor to ridemode is given
            String currRange = calculateRangeUsingSOC(currStateOfCharge,rideModeStr);
            int vehRangeProg = 0;
            vehRangeProg = ((Integer.parseInt(currRange) * 100) / maxRangeVal);
            String curVehPer = String.valueOf(Math.round(vehRangeProg));
            seekBar.setProgress(Integer.parseInt(curVehPer));
            txtdistance.setText(currRange + " km");
        });
        reverseModeModel.getData().observe(this,newData->{
            // Update UI components with the new data
           String revMode = newData;
            if(revMode.equalsIgnoreCase("true")||revMode.equalsIgnoreCase("ON")){
                reverseMode = "ON";
            }
            else{
                reverseMode = "OFF";
            }
        });
        ignitionModel.getData().observe(this,newData->{
            // Update UI components with the new data
            String ignitionMode = newData;
            if(ignitionMode.equalsIgnoreCase("true")||ignitionMode.equalsIgnoreCase("ON")){
                ignitionStr = "ON";
            }
            else{
                ignitionStr = "OFF";
            }
        });

        dataReceiveModel.getData().observe(this,newData->{
            dataReceived = newData.equalsIgnoreCase("true");
        });

        Log.d("valueee",mqttDbHandler.getRowsCount()+"");
        int pendingRowsCount = mqttDbHandler.getRowsCount();
        //Upload pending Data
        new MqttDataUpload(mqttDbHandler,MainActivity.this).execute();
        /*if(pendingRowsCount > 0){
            //checking for internet connection.
            ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
            //No internet connection
            if (netInfo == null){
                //Do Nothing
            }
            //internet Connected
            else{
                //Upload pending Data
                new MqttDataUpload(mqttDbHandler,MainActivity.this).execute();
            }
        }*/

        try{
            //Initially connecting to MQTT client and establishing the connection
            //Setup MQTT Connection
            client = new MqttClient(broker, clientid, new MemoryPersistence());
            options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setConnectionTimeout(30);
            options.setKeepAliveInterval(60);
            options.setAutomaticReconnect(false);
            mqttConnectTask = new MqttConnect();
            mqttConnectTask.execute();
            client.setCallback(this);
            //options.setExecutorServiceTimeout();
        }
        catch (MqttException e){
            e.printStackTrace();
        }
    }

    public class MqttConnect extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // connect
                Log.d("mqtt", "attemting to connect " + System.currentTimeMillis());
                client.connect(options);
                Log.d("mqtt", "connected" + System.currentTimeMillis());
                client.setCallback(MainActivity.this);
            }
            catch (MqttException e){
                Log.d("exception",e.getMessage());
                e.printStackTrace();
                retryMqttConnect();
            }
            return null;
        }
    }

    public void retryMqttConnect(){
        if(mqttConnectTask.getStatus() == AsyncTask.Status.RUNNING ||
                mqttConnectTask.getStatus() == AsyncTask.Status.PENDING) {
            mqttConnectTask.cancel(true);
            mqttConnectTask = new MqttConnect();
            mqttConnectTask.execute();
        }
    }






    @SuppressLint("UseCompatLoadingForDrawables")
    public void init(){
        //Initilization
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
        vehicle_soc=findViewById(R.id.vehicle_soc);
        vehicle_chargeBat=findViewById(R.id.imageView8);
        chargingbar=findViewById(R.id.landscapeProgressWidgetCharging);
        chargingbarBattery=findViewById(R.id.landscapeProgressWidgetChargingbat);
        mProgressView = findViewById(R.id.gradiant_progress);
        linearLayout_cluster = findViewById(R.id.linearLayout_cluster);
        linearLayoutbat = findViewById(R.id.linearLayoutbat);
        mqttUploadStatusImg = findViewById(R.id.mqtt_upload);
        //mqttUploadStatusImg.setBackground(getDrawable(R.drawable.upload));
        Glide.with(MainActivity.this).load(R.drawable.upload).into(mqttUploadStatusImg);

        leftBlinking = false;
        rightBlinking = false;
        hazardBlinking = false;

        //MQTT Data base initilization
        mqttDbHandler = new MqttDBHelper(MainActivity.this);
        mqttDbHandler.createIfNotExists();
        mqttDbHandler.close();

        //Location Database initilization
        locationDBHandler = new LocationDBHandler(MainActivity.this);
        locationDBHandler.createIfNotExists();
        locationDBHandler.close();
        //Statistics Database initilization
        statisticsDBHandler = new StatisticsDBHandler(MainActivity.this);
        statisticsDBHandler.createIfNotExists();
        statisticsDBHandler.close();


        //initilization of MutableLiveData variables
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
        ignitionModel = new ViewModelProvider(this).get(IgnitionModel.class);
        dataReceiveModel = new ViewModelProvider(this).get(DataReceiveModel.class);
        //Animation initilization
        animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);

        neumorphCardView = findViewById(R.id.rlByDrive);

    }

    public static int math(float f) {
        int c = (int) ((f) + 0.5f);
        float n = f + 0.5f;
        return (n - c) % 2 == 0 ? (int) f : c;
    }

    //Calculating the charging time
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

    // function to Create MQTT data
    @Synchronized
    public void constructCurrMqttPacket(){
        currentPacketContent = "";
        try{
            DateTimeFormatter curDate = DateTimeFormatter.ofPattern("ddMMyyyy");
            DateTimeFormatter curTime = DateTimeFormatter.ofPattern("hhmmss");
            LocalDateTime now = LocalDateTime.now();
            networkDate = curDate.format(now);
            networkTime = curTime.format(now);

            date = networkDate;
            time = networkTime;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        frameNumber = String.valueOf(Integer.parseInt(frameNumber)+1);
        Log.d("frameNo",frameNumber);
        obdData="ON|SEQ_PHASED|PHASED|"+ vehicleErrorIndication +"|[1126;0;0;0;0;0]|"+ rideMode +"|1095|29.000|"+ vehicleCharge +"|"+ regenerationActive +"|2.334|0|0|19.938|36|102.719|86.938|"+ vehicleRange +
                "|312|0.513|0.160|0.160|0.547|1.000|0.000|[41;0;0;0;0;0]|[0.000;0;0;0;0;0]|12.930|5.676|"+ vehicleChargingTime +"|0.505|6.383|2.364|6.562|-1.000|0.000|36.719|0.938|2.404|"
                + reverseMode +"|0.000|"+ currStateOfCharge +"|"+ speedometer +"|-3.100|12420.60|"+ batterySOH +"|8.00|12.95|[0;0;0;0;0;0;0;0;0]|0.00|1.00|0.00|1.00|0.00|0.00|0.00|0.00";

        content = "{\"payload\":\"$,RE-CONNECT,506.6,4.4,V9.4,"+ignitionStr+","+alertId+","+packetStatus+",555555555510200,"+stGPSValidity+"," +
                ""+date+","+time+","+latitude+","+latitudeDir+","+longitude+","+longitudeDir+",0.0,0,0,0,0.0,0.0,airtel,"+ignitionStatus+",12.31,"+gsmSignalStrength+
                ",1,"+frameNumber+",0,"+obdData+",,P0030,ME3EVMULE02TEST01,"+tripId+",M4A,*4e\"}";

        Log.d("string_val",content);

        currMqttPacketUpload = true;
        currentPacketContent = content;

    }



    //Save MQTT data in local DB
    public void save_mqtt_records(String rawData,String uploadStatus,String timestamp){
        mqttDbHandler.addMqttData(rawData, uploadStatus, timestamp);
    }

    //Location listener to get currect location on location change
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

        //fetching location using GPS
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

        //NMEA loation listener
        locationManager.requestSingleUpdate(criteria, locationListener, null);
        locationManager.addNmeaListener(new OnNmeaMessageListener() {
            @Override
            public void onNmeaMessage(String s, long l) {
                try {
                    //Log.d(TAG,"Nmea Received :");
                    //Log.d(TAG,"Timestamp is :" +l+"   nmea is :"+s);

                    //$GNRMC,075030.00,A,1254.161924,N,08013.621093,E,0.0,,150923,1.1,W,A,V*6E
                    //getting location latitude and longitude
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
                    //getting location date and time
                    if (s.contains("$GNRMC")) {
                        String[] gpsNmea = s.split(",");
                        //Log.d("valuesss",gpsNmea[1]+"\t"+gpsNmea[9]);
                        if(!gpsNmea[9].equalsIgnoreCase("")) {
                            date = outputDate.format(inputDate.parse(gpsNmea[9]));
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

    //setting the call back service for MQTT client to check connection lost and msg delivery status

    //Vechicle range logic
    public String calculateRangeUsingSOC(String currSOC,String currRideMode){
        double currVehicleRange = 0;
        String vehicleRangeStr = "";
        maxRangeVal = 0;
        int curSOCInt = Integer.parseInt(currSOC);
        if(currRideMode.equalsIgnoreCase("ECO")){
            double ecoFactor = 2.0;
            currVehicleRange = ecoFactor * curSOCInt;
            maxRangeVal = (int)Math.round(ecoFactor * 100);
        }else if(currRideMode.equalsIgnoreCase("TOUR")){
            double tourFactor = 1.8;
            currVehicleRange = tourFactor * curSOCInt;
            maxRangeVal = (int)Math.round(tourFactor * 100);
        }else if(currRideMode.equalsIgnoreCase("SPORTS")){
            double sportFactor = 1.5;
            currVehicleRange = sportFactor * curSOCInt;
            maxRangeVal = (int)Math.round(sportFactor * 100);
        }
        vehicleRangeStr = String.valueOf(Math.round(currVehicleRange));
        return vehicleRangeStr;
    }

    public void calculateLowSOC(String currLowSOC,String currSOC){
        int currLowSocInt = Integer.parseInt(currLowSOC);
        int currSOCInt = Integer.parseInt(currSOC);
        if(currSOCInt <= currLowSocInt){
            vehicle_soc.setBackground(getDrawable(R.drawable.soc_low));
            //Glide.with(MainActivity.this).load(R.drawable.soc_low).into(vehicle_soc);
        }
        else{
            vehicle_soc.setBackground(getDrawable(R.drawable.soc));
            //Glide.with(MainActivity.this).load(R.drawable.soc).into(vehicle_soc);
        }
    }


    public void calulateODODistance(int mCurrSpeedVal){
        long mCurrentTimeMs = System.currentTimeMillis();
        double currDistance = 0;

        if(mPrevTimeMs == 0){
            mPrevTimeMs = mCurrentTimeMs;
        }
        if(mCurrentTimeMs > mPrevTimeMs) {
            //currSpeedValm has to be typecasted to double so that decimal values be captured
            currDistance = ((mCurrentTimeMs - mPrevTimeMs) * (double) mCurrSpeedVal) / (3600 * 1000);
            Log.d("currDist",currDistance+"\t");
            totalDistance = totalDistance + currDistance;
            Log.d("currDist",currDistance+"\t"+totalDistance);
            mPrevTimeMs = mCurrentTimeMs;

            txtodo.setCharacterLists(TickerUtils.provideNumberList());
            txtodo.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);
            Typeface typeface = ResourcesCompat.getFont(MainActivity.this, R.font.audiowide_regular);
            txtodo.setTypeface(typeface);
            txtodo.setGravity(Gravity.CENTER);
            //txtodo.setText(String.valueOf((int)totalDistance));
            long totDist = Math.round(totalDistance);
            String padded = String.format("%0"+odo_pad_width+"d", Integer.parseInt(String.valueOf(totDist)));
            txtodo.setText(padded);

            if (statisticsDataCursor.getCount() == 0) {
                lastODOTimestamp = System.currentTimeMillis();
                //Log.d("locationc",latitude+"\t"+longitude);
                statisticsDBHandler.addNewOdo(String.valueOf(totDist), String.valueOf(lastODOTimestamp));
                statisticsDataCursor = statisticsDBHandler.getAllData();
            } else {
                lastODOTimestamp = System.currentTimeMillis();
                //Log.d("locationu",latitude+"\t"+longitude);
                statisticsDBHandler.updateODO(String.valueOf(totDist), String.valueOf(lastODOTimestamp));
            }
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        mqttConnect = true;
        Log.d("mqttStatus","connection");
    }

    @Override
    public void connectionLost(Throwable cause) {
        mqttConnect = false;
        Log.d("mqttStatus","lostconnection");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d("mqttStatus","message");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d("mqttStatus","Packet Upload successfully");
    }


    /**********************************************************************************************
     *
     * MQTT DATA UPLOAD ASYNC TASK will perform 2 main activities
     * 1. The Task is configured to run continuosly at every mqttServerAsyncTaskDelay
     * 2. The Delay is configured to be a subset of mqttServerUploadDelay
     * 3. The Task will first check if there is a current packet for upload
     * 4. When there is no current packet then it uploads a pending packet.
     *
     *********************************************************************************************/
    public class MqttDataUpload extends AsyncTask<Void,Void,Void> {

        MqttDBHelper mqttDBHelper;
        Context context;
        String topic = "gprsData";
        String pendingContent = "";
        int qos = 0;
        Handler handler = new Handler();
        Runnable runnable;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        public MqttDataUpload(MqttDBHelper mqttDBHelper, Context context) {
            this.mqttDBHelper = mqttDBHelper;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            handler.postDelayed(runnable = new Runnable() {
                public void run() {
                    //checking for internet connection.
                    ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
                    //No internet connection
                    if (netInfo == null){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                //mqttUploadStatusImg.setBackground(getDrawable(R.drawable.fail_upload));
                                Glide.with(MainActivity.this).load(R.drawable.fail_upload).into(mqttUploadStatusImg);
                            }
                        });
                        if(currMqttPacketUpload){
                            Log.d("before3",currentPacketContent);
                            String contentMarkHistory = currentPacketContent.replace("1,L,","1,H,");
                            Log.d("before3",contentMarkHistory);
                            save_mqtt_records(contentMarkHistory,"0",String.valueOf(System.currentTimeMillis()));
                            currMqttPacketUpload = false;
                        }

                    }
                    else {
                        //Network connection available
                        //STEP1: Check if there is a current packet to be uploaded
                        //Current Packet has higher priority than Pending PAckets
                        //The Flag currMqttPacketUpload is set in the main activity at packet construction interval.
                        if(mqttConnect) {
                            runOnUiThread(new Runnable() {
                                @SuppressLint("UseCompatLoadingForDrawables")
                                public void run() {
                                    //mqttUploadStatusImg.setBackground(getDrawable(R.drawable.fail_upload));
                                    //Update ICon on UI to denote failed Upload
                                    Glide.with(MainActivity.this).load(R.drawable.upload).into(mqttUploadStatusImg);
                                }
                            });
                            if (currMqttPacketUpload) {
                                try {
                                    //Only publishing is done here
                                    // publish message
                                    Log.d("dataUp", "current");
                                    //Since we are switching to current packet upload, decrement pending upload count
                                    MqttMessage message = new MqttMessage(currentPacketContent.getBytes());
                                    message.setQos(qos);
                                    client.publish(topic, message);
                                    //Save Data in DB with Status as 1
                                    save_mqtt_records(currentPacketContent, "1", String.valueOf(System.currentTimeMillis()));
                                    //Reset Flag to denote upload Completed
                                    currMqttPacketUpload = false;

                                    System.out.println("Message published cur");
                                    System.out.println("topic: " + topic);
                                    System.out.println("message content: " + currentPacketContent);

                                } catch (MqttException e) {
                                    //Incase of Exception, save the packate in DB and Reset Flag
                                    // This Exception typically happens when the server is unresponsive and the MQTT connection Fails
                                    currMqttPacketUpload = false;
                                    e.printStackTrace();
                                    Log.d("error", e.getMessage());
                                    Log.d("before2",currentPacketContent);
                                    String contentMarkHistory = currentPacketContent.replace("1,L,","1,H,");
                                    Log.d("before2",contentMarkHistory);
                                    save_mqtt_records(contentMarkHistory, "0", String.valueOf(System.currentTimeMillis()));
                                    e.printStackTrace();
                                    Log.d("error", e.getMessage());
                                    // Glide.with(MainActivity.this).load(R.drawable.fail_upload).into(mqttUploadStatusImg);
                                    runOnUiThread(new Runnable() {
                                        @SuppressLint("UseCompatLoadingForDrawables")
                                        public void run() {
                                            //mqttUploadStatusImg.setBackground(getDrawable(R.drawable.fail_upload));
                                            //Update ICon on UI to denote failed Upload
                                            Glide.with(MainActivity.this).load(R.drawable.fail_upload).into(mqttUploadStatusImg);
                                        }
                                    });
                                }
                            } else {
                                //There is no Current Packet for Upload. So Check for Pending Packets
                                //check for pending packet in DB. Read One Packet at a Time.
                                // The DB read is configured to read multiple packets if required and hence the array.
                                // Right Now we are returning only one packet and hence the array size will be 1
                                ArrayList<MqttDataModel> mqttDataModelArrayList = mqttDBHelper.readMqttData();
                                if (mqttDataModelArrayList.size() > 0) {
                                    //login when pending packet are there in DB
                                    //for loop can be removed later
                                    for (int i = 0; i < mqttDataModelArrayList.size(); i++) {
                                        pendingContent = mqttDataModelArrayList.get(i).getRaw_data();
                                        try {
                                            //Only publishing is done here
                                            // create message and setup QoS
                                            // publish message
                                            Log.d("dataUp", "pending");
                                            MqttMessage message = new MqttMessage(pendingContent.getBytes());
                                            message.setQos(qos);
                                            // The Publish call is syncronous and will retrun for completing the publish.
                                            client.publish(topic, message);
                                            // Once the Pending Packet is uploaded, Update Status as 1.
                                            mqttDBHelper.updateCourse(mqttDataModelArrayList.get(i).getId_col());
                                            System.out.println("Message published");
                                            System.out.println("topic: " + topic);
                                            System.out.println("message content: " + pendingContent);

                                        } catch (MqttException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            }
                        }


                        else if(!mqttConnect) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Glide.with(MainActivity.this).load(R.drawable.fail_upload).into(mqttUploadStatusImg);
                                }
                            });
                            if(currMqttPacketUpload){
                                Log.d("before1",currentPacketContent);
                                String contentMarkHistory = currentPacketContent.replace("1,L,","1,H,");
                                Log.d("before1",contentMarkHistory);
                                save_mqtt_records(contentMarkHistory,"0",String.valueOf(System.currentTimeMillis()));
                                currMqttPacketUpload = false;
                            }
                        }
                    }
                    handler.postDelayed(runnable, mqttServerAsyncTaskDelay);
                }
            }, mqttServerAsyncTaskDelay);

            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            // disconnect
            Log.d("Pending_mqtt", "Disconnected");
            client.disconnect();
            // close client
            client.close();
        }
        catch (MqttException e){
            e.printStackTrace();
        }
    }







}