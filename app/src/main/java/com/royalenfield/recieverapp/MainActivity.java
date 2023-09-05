package com.royalenfield.recieverapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
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

    ImageView right_on;
    ImageView left_on;
    ImageView hazard_on;
    ImageView error_on;
    ImageView regen_on;
    ImageView abs_on;

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


    private MyBroadcastReceiver MyReceiver;
    public static final String CUSTOM_ACTION = "com.royalenfield.evcansim.CUSTOM_ACTION";
    private  boolean dataReceived = false;

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
        rightstr=findViewById(R.id.imageView7);
        leftstr=findViewById(R.id.imageView6);
        hazardstr=findViewById(R.id.imageView2);
        errorstr=findViewById(R.id.imageView3);
        regenstr=findViewById(R.id.imageView);
        abs_str=findViewById(R.id.imageView5);
        chargingbar=findViewById(R.id.landscapeProgressWidgetCharging);

        right_on=findViewById(R.id.right_on);
        left_on=findViewById(R.id.left_on);
        hazard_on=findViewById(R.id.hazard_ON);
        error_on=findViewById(R.id.dtcON);
        regen_on=findViewById(R.id.powerON);
        abs_on=findViewById(R.id.absON);

        txtodo.setCharacterLists(TickerUtils.provideNumberList());
        txtodo.setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN);

        //Receive data which has been broadcasted
        IntentFilter filter = new IntentFilter("com.royalenfield.evcansim.CUSTOM_ACTION");
        MyReceiver = new MyBroadcastReceiver();
        registerReceiver(MyReceiver, filter);

        new CountDownTimer(10000, 1000) {
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
        }.start();
    }

    /**
     * Function for receiving Broadcast
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(CUSTOM_ACTION)) {
                    try {
                        JSONObject jsonObject = new JSONObject(intent.getStringExtra(""));

                        if (jsonObject.getString("signal").contains("speed")) {
                            speedometer = jsonObject.getString("data");
                            txtspeed.setText(speedometer);
                            speedoMeterView.setSpeed(Integer.parseInt(speedometer)/2,true);
                        } else if (jsonObject.getString("signal").contains("vehicle_range")) {
                            vehicleRange = jsonObject.getString("data");
                            seekBar.setProgress(Integer.parseInt(vehicleRange));
                            txtdistance.setText(vehicleRange + " km");
                        } else if (jsonObject.getString("signal").contains("odo")) {
                            vehicleOdometer = jsonObject.getString("data");
                            txtodo.setText(vehicleOdometer);
                        } else if (jsonObject.getString("signal").contains("charging_status")) {
                            vehicleCharge = jsonObject.getString("data");
                        } else if (jsonObject.getString("signal").contains("charging_time")) {
                            vehicleChargingTime = jsonObject.getString("data");
                        } else if (jsonObject.getString("signal").contains("soc")) {
                            stateOfCharge = jsonObject.getString("data");
                            Log.d("chargeeee",stateOfCharge);
                            if (stateOfCharge.equalsIgnoreCase("0")){
                            }
                            else {
                                txtvehiclechrg.setText(stateOfCharge+"%");
                                chargingbar.setPercentage(Integer.parseInt(stateOfCharge));
                            }
                        } else if (jsonObject.getString("signal").contains("soc_low")) {
                            LowSoc = jsonObject.getString("data");
                        } else if (jsonObject.getString("signal").contains("battery_soh")) {
                            batterySOH = jsonObject.getString("data");
                        } else if (jsonObject.getString("signal").contains("right_ttl")) {
                            right = jsonObject.getString("data");
                            if (right.equalsIgnoreCase("true")){
                                rightstr.setVisibility(View.GONE);
                                right_on.setVisibility(View.VISIBLE);
                            } else if (right.equalsIgnoreCase("false")) {
                                rightstr.setVisibility(View.VISIBLE);
                                right_on.setVisibility(View.GONE);
                            }
                        } else if (jsonObject.getString("signal").contains("left_ttl")) {
                            left = jsonObject.getString("data");
                            if (left.equalsIgnoreCase("true")){
                                leftstr.setVisibility(View.GONE);
                                left_on.setVisibility(View.VISIBLE);
                            } else if (left.equalsIgnoreCase("false")) {
                                leftstr.setVisibility(View.VISIBLE);
                                left_on.setVisibility(View.GONE);
                            }
                        } else if (jsonObject.getString("signal").contains("hazard_ttl")) {
                            hazard = jsonObject.getString("data");
                            if (hazard.equalsIgnoreCase("true")){
                                hazardstr.setVisibility(View.GONE);
                                hazard_on.setVisibility(View.VISIBLE);
                            } else if (hazard.equalsIgnoreCase("false")) {
                                hazardstr.setVisibility(View.VISIBLE);
                                hazard_on.setVisibility(View.GONE);
                            }
                        } else if (jsonObject.getString("signal").contains("vehicle_error_ind")) {
                            vehicleErrorIndication = jsonObject.getString("data");
                            if (vehicleErrorIndication.equalsIgnoreCase("true")){
                                errorstr.setVisibility(View.GONE);
                                error_on.setVisibility(View.VISIBLE);
                            } else if (vehicleErrorIndication.equalsIgnoreCase("false")) {
                                errorstr.setVisibility(View.VISIBLE);
                                error_on.setVisibility(View.GONE);
                            }
                        } else if (jsonObject.getString("signal").contains("regen_active")) {
                            regenerationActive = jsonObject.getString("data");
                            if (regenerationActive.equalsIgnoreCase("true")){
                                regenstr.setVisibility(View.GONE);
                                regen_on.setVisibility(View.VISIBLE);
                            } else if (regenerationActive.equalsIgnoreCase("false")) {
                                regenstr.setVisibility(View.VISIBLE);
                                regen_on.setVisibility(View.GONE);
                            }
                        } else if (jsonObject.getString("signal").contains("abs_active")) {
                            absEvent = jsonObject.getString("data");
                            if (absEvent.equalsIgnoreCase("0")){
                                abs_str.setVisibility(View.GONE);
                                abs_on.setVisibility(View.VISIBLE);
                            } else if (absEvent.equalsIgnoreCase("1")) {
                                abs_str.setVisibility(View.VISIBLE);
                                abs_on.setVisibility(View.GONE);
                            }
                        }else if(jsonObject.getString("signal").contains("riding_mode")){
                            rideMode=jsonObject.getString("data");
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
                        }else if(jsonObject.getString("signal").contains("reverse_mode")){
                            reverseMode=jsonObject.getString("data");
                        }

                        dataReceived = true;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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