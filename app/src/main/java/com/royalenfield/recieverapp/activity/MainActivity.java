package com.royalenfield.recieverapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.progress.progressview.ProgressView;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;
import com.royalenfield.recieverapp.R;
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

public class MainActivity extends AppCompatActivity {

    TextView txtspeed;
    TextView txtdistance;
    TextView txtmodes;
    TickerView txtodo;
    SpeedoMeterView speedoMeterView;
    SeekBar seekBar;
    LandscapeProgressWidgetCharging chargingbar;

    TextView txtvehiclechrg;
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

    public static String LowSoc, right, left, hazard;

    String broker = "tcp://35.200.186.3:1883";
    String topic = "gprsData";
    String username = "royalenfield";
    String password = "RoyalEnfieldMqttBroker";
    String clientid = "publish_client";
    String content = "Hello MQTT";

    int qos = 0;

    String packetType="NR",alertId="2",packetStatus="H",date="29102023",time="183507",latitude="12.975843",latitudeDir="N",longitude="77.549438",longitudeDir="E",ignitionStatus="1";
    String gsmSignalStrength="31",frameNumber="8273",obdData,tripId="1";

    String vehicleErrorIndication="ON", rideMode="Sports", vehicleCharge="Complete", regenerationActive="ON", vehicleRange="120", vehicleChargingTime="300",
            reverseMode="ON", stateOfCharge="100", speedometer="80", batterySOH="85",absEvent="0", vehicleOdometer="1520";
    private  boolean dataReceived = false;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gauge_layout);

        //remove bottom navigation
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        dataReceived = false;


        txtspeed = findViewById(R.id.speed);
        txtdistance = findViewById(R.id.rangeTxt);
        txtodo = findViewById(R.id.odoMeter);
        speedoMeterView=findViewById(R.id.speedometerview);
        seekBar=findViewById(R.id.seekBar);
        txtmodes=findViewById(R.id.txtmodes);
        txtvehiclechrg=findViewById(R.id.chargePercent);
        rightstr=findViewById(R.id.right_ind);
        leftstr=findViewById(R.id.left_ind);
        hazardstr=findViewById(R.id.vehicle_hazard);
        errorstr=findViewById(R.id.vehicle_error);
        regenstr=findViewById(R.id.vehicle_regen);
        abs_str=findViewById(R.id.vehicle_abs);
        vehicle_charge=findViewById(R.id.vehicle_charge);
        chargingbar=findViewById(R.id.landscapeProgressWidgetCharging);
        mProgressView = findViewById(R.id.gradiant_progress);

        txtodo.setCharacterLists(TickerUtils.provideNumberList());
        txtodo.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);

        /*new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                if(dataReceived) {
                    new UploadData().execute();
                }
            }

            @Override
            public void onFinish() {
                start();
            }
        }.start();*/
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
            Log.d("valueee",Integer.parseInt(speedometer) / 2+"");
            colorList = new int[]{Color.argb((float) 0.1, 0, 0, 0), Color.rgb(57, 189, 189)};

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
            txtodo.setText(vehicleOdometer);
        });

        chargingStatusModel.getData().observe(this,newData->{
            vehicleCharge = newData;
            vehicle_charge.setVisibility(View.VISIBLE);
            if (vehicleCharge.equalsIgnoreCase("DISABLED")) {
                Glide.with(getApplicationContext()).load(R.drawable.charge).into(vehicle_charge);
            } else if (vehicleCharge.equalsIgnoreCase("STATIC")) {
                Glide.with(getApplicationContext()).load(R.drawable.charge_error).into(vehicle_charge);
            } else if (vehicleCharge.equalsIgnoreCase("FREQ")) {
                Glide.with(getApplicationContext()).load(R.drawable.charge_on).into(vehicle_charge);
            } else if (vehicleCharge.equalsIgnoreCase("OUTPUT")) {
                Glide.with(getApplicationContext()).load(R.drawable.charge_complete).into(vehicle_charge);
            }
        });

        chargingTimeModel.getData().observe(this,newData->{
            vehicleChargingTime = newData;
        });

        socModel.getData().observe(this,newData->{
            stateOfCharge = newData;
            //Log.d("chargeeee",stateOfCharge);
            if (stateOfCharge.equalsIgnoreCase("0")){
            }
            else {
                txtvehiclechrg.setText(stateOfCharge+"%");
                chargingbar.setPercentage(Integer.parseInt(stateOfCharge));
            }
        });

        lowSOCModel.getData().observe(this,newData->{
            LowSoc = newData;
        });

        batterySOHModel.getData().observe(this,newData->{
            batterySOH = newData;
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
                Glide.with(getApplicationContext()).load(R.drawable.dtc_on).into(errorstr);
            } else if (vehicleErrorIndication.equalsIgnoreCase("false")) {
                Glide.with(getApplicationContext()).load(R.drawable.dtc).into(errorstr);
            }
        });

        regenModel.getData().observe(this,newData->{
            regenerationActive = newData;
            if (regenerationActive.equalsIgnoreCase("true")){
                Glide.with(getApplicationContext()).load(R.drawable.power_on).into(regenstr);
            } else if (regenerationActive.equalsIgnoreCase("false")) {
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
                rideMode = "Eco";
                txtmodes.setText(rideMode);

            } else if (rideMode.equalsIgnoreCase("ST")) {
                rideMode = "Tour";
                txtmodes.setText(rideMode);

            } else if (rideMode.equalsIgnoreCase("IS")) {
                rideMode = "Sports";
                txtmodes.setText(rideMode);
            }
        });
        reverseModeModel.getData().observe(this,newData->{
            reverseMode = newData;
        });


    }

    public class UploadData extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... voids) {
            obdData="ON|SEQ_PHASED|PHASED|"+ vehicleErrorIndication +"|[1126;0;0;0;0;0]|"+ rideMode +"|1095|29.000|"+ vehicleCharge +"|"+ regenerationActive +"|2.334|0|0|19.938|36|102.719|86.938|"+ vehicleRange +
                    "|312|0.513|0.160|0.160|0.547|1.000|0.000|[41;0;0;0;0;0]|[0.000;0;0;0;0;0]|12.930|5.676|"+ vehicleChargingTime +"|0.505|6.383|2.364|6.562|-1.000|0.000|36.719|0.938|2.404|"
                    + reverseMode +"|0.000|"+ stateOfCharge +"|"+ speedometer +"|-3.100|12420.60|"+ batterySOH +"|8.00|12.95|[0;0;0;0;0;0;0;0;0]|0.00|1.00|"+absEvent+"|1.00|0.00|0.00|0.00|0.00|0.00|0.00|0.00|0.00|0.00|"+ vehicleOdometer;

            content = "{\"payload\":\"$,RE-CONNECT,506.6,4.4,V9.4,"+packetType+","+alertId+","+packetStatus+",660906045499651,1," +
                    ""+date+","+time+","+latitude+","+latitudeDir+","+longitude+","+longitudeDir+",0.0,0,0,0,0.0,0.0,airtel,"+ignitionStatus+",12.31,"+gsmSignalStrength+
                    ",1,"+frameNumber+",0,"+obdData+",,P0030,ME3J3A5FBM2005446,"+tripId+",M4A,*4e\"}";

            Log.d("string_val",content);

            return content;
        }

        @Override
        protected void onPostExecute(String contentVal) {
            super.onPostExecute(contentVal);
            try {
                MqttClient client = new MqttClient(broker, clientid, new MemoryPersistence());
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(username);
                options.setPassword(password.toCharArray());
                options.setConnectionTimeout(60);
                options.setKeepAliveInterval(60);
                // connect
                client.connect(options);
                // create message and setup QoS
                MqttMessage message = new MqttMessage(contentVal.getBytes());
                message.setQos(qos);
                // publish message
                client.publish(topic, message);
                System.out.println("Message published");
                System.out.println("topic: " + topic);
                System.out.println("message content: " + contentVal);
                // disconnect
                client.disconnect();
                // close client
                client.close();
            }
            catch (MqttException e){
                e.printStackTrace();
            }
        }

    }
}