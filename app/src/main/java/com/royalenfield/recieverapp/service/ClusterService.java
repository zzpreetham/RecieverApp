package com.royalenfield.recieverapp.service;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.royalenfield.recieverapp.activity.MainActivity;
import com.royalenfield.recieverapp.liveDataModel.SpeedModel;

import org.json.JSONObject;

public class ClusterService extends Service{

    private MyBroadcastReceiver MyReceiver;
    private double speed;
    private double soc;
    public static final String CUSTOM_ACTION = "com.royalenfield.digital.telemetry.info.ACTION_SEND";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Receive data which has been broadcasted
        IntentFilter filter = new IntentFilter("com.royalenfield.digital.telemetry.info.ACTION_SEND");
        MyReceiver = new MyBroadcastReceiver();
        registerReceiver(MyReceiver, filter);
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(CUSTOM_ACTION)) {
                    try {
                        //Parsing data logic to Cluster UI Using MutableLiveData
                        //Once we receive the parameters based on KEY passing to MAIN activity

                        //Log.d("received_can",jsonObject.toString());
                        /*double tempSpeed = intent.getDoubleExtra("speed", -1.0);
                        Log.d("Rec_speed",tempSpeed+"");
                        if( tempSpeed != -1.0) {

                            speed = tempSpeed;
                            speed = speed * 100;
                            speed = Math.round(speed);
                            speed = speed / 100;
                            //Below line for MutableLiveDataParsing
                            MainActivity.speedModel.updateData(String.valueOf((int)speed));
                        }

                        double tempSoc =  intent.getDoubleExtra("soc", -1.0);
                        Log.d("MyBroadcastReceiver", "tempsoc: " + tempSoc);
                        if( tempSoc != -1.0 ) {
                            soc = tempSoc;
                            soc = soc *100;
                            soc = Math.round(soc);
                            soc = Math.round(soc);
                            soc = Math.round(soc);
                            soc = soc /100;
                            MainActivity.socModel.updateData(String.valueOf((int)soc));
                        }*/
                        JSONObject jsonObject = new JSONObject(intent.getStringExtra(""));
                        if (jsonObject.getString("signal").contains("speed")) {
                            MainActivity.speedModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("vehicle_range")) {
                            MainActivity.distanceModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("odo")) {
                            MainActivity.odoModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("charging_status")) {
                            MainActivity.chargingStatusModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("charging_time")) {
                            MainActivity.chargingTimeModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").equals("soc")) {
                            MainActivity.socModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").equals("soc_low")) {
                            MainActivity.lowSOCModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("battery_soh")) {
                            MainActivity.batterySOHModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("right_ttl")) {
                            MainActivity.rightTTLModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("left_ttl")) {
                            MainActivity.leftTTLModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("hazard_ttl")) {
                            MainActivity.hazardTTLModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("vehicle_error_ind")) {
                            MainActivity.vehicleErrorModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("regen_active")) {
                           MainActivity.regenModel.updateData(jsonObject.getString("value"));
                        }
                        else if (jsonObject.getString("signal").contains("abs_active")) {
                            MainActivity.absModel.updateData(jsonObject.getString("value"));
                        }
                        else if(jsonObject.getString("signal").contains("riding_mode")){
                            MainActivity.ridingModeModel.updateData(jsonObject.getString("value"));
                        }
                        else if(jsonObject.getString("signal").contains("reverse_mode")){
                            MainActivity.reverseModeModel.updateData(jsonObject.getString("value"));
                        }


                        //MainActivity.dataReceived = true;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
