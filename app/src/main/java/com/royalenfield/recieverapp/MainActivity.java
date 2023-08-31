package com.royalenfield.recieverapp;

import static com.royalenfield.recieverapp.MessageReceiver.CUSTOM_ACTION;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView txtspeed;
    TextView txtdistance;
    TextView txtodo;
    TextView txtvehiclechrg;
    TextView txtchrgtym;
    TextView txtsoc;
    TextView txtLowSoc;
    TextView txtbatterysoh;
    TextView rightstr;
    TextView leftstr;
    TextView hazardstr;
    TextView errorstr;
    TextView regenstr;
    TextView abs_str;

    public static String speed, distance, odo, vehiclechrg, chrgtym, soc, LowSoc, batterysoh,
            right, left, hazard, error, regen, abs = "";

    private MyBroadcastReceiver MyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txtspeed = findViewById(R.id.speed_change);
        txtdistance = findViewById(R.id.distance_change);
        txtodo = findViewById(R.id.odo_change);
        txtvehiclechrg = findViewById(R.id.charge_state);
        txtchrgtym = findViewById(R.id.charge_time);
        txtsoc = findViewById(R.id.vehicle_soc);
        txtLowSoc = findViewById(R.id.vehicle_low_soc);
        txtbatterysoh = findViewById(R.id.battery_SOH);
        rightstr = findViewById(R.id.right_switch);
        leftstr = findViewById(R.id.left_switch);
        hazardstr = findViewById(R.id.hazard_switch);
        errorstr = findViewById(R.id.vehicle_err_switch);
        regenstr = findViewById(R.id.regen_switch);
        abs_str = findViewById(R.id.abs_switch);

        IntentFilter filter = new IntentFilter("com.royalenfield.evcansim.CUSTOM_ACTION");
        MyReceiver = new MyBroadcastReceiver();
        registerReceiver(MyReceiver, filter);

    }


    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(CUSTOM_ACTION)) {

                    //Log.d("valueeeee",intent.getStringExtra("SignalPacket"));

                    try {
                        JSONObject jsonObject = new JSONObject(intent.getStringExtra(""));
                        Log.d("jsonVal12344", jsonObject.toString());
                        Log.d("jsonVal", jsonObject.getString("signal") + "\t" + jsonObject.getString("data") + "\t" + jsonObject.getString("timestamp"));


                        if (jsonObject.getString("signal").contains("speed")) {
                            //Log.d("speeedddd", jsonObject.getString("speed"));
                            speed = jsonObject.getString("data");
                            txtspeed.setText(speed + " kmph");
                        } else if (jsonObject.getString("signal").contains("distance")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            distance = jsonObject.getString("data");
                            txtdistance.setText(distance + " km");
                        } else if (jsonObject.getString("signal").contains("odo")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            odo = jsonObject.getString("data");
                            txtodo.setText(odo + " km");
                        } else if (jsonObject.getString("signal").contains("vehiclechrg")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            vehiclechrg = jsonObject.getString("data");
                            txtvehiclechrg.setText(vehiclechrg + "%");
                        } else if (jsonObject.getString("signal").contains("chrgtym")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            chrgtym = jsonObject.getString("data");
                            txtchrgtym.setText(chrgtym + " min");
                        } else if (jsonObject.getString("signal").contains("soc")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            soc = jsonObject.getString("data");
                            txtsoc.setText(soc);
                        } else if (jsonObject.getString("signal").contains("lowSOC")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            LowSoc = jsonObject.getString("data");
                            txtLowSoc.setText(LowSoc + " %");
                        } else if (jsonObject.getString("signal").contains("batterySOH")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            batterysoh = jsonObject.getString("data");
                            txtbatterysoh.setText(batterysoh + " %");
                        } else if (jsonObject.getString("signal").contains("rightIND")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            right = jsonObject.getString("data");
                            rightstr.setText(right);
                        } else if (jsonObject.getString("signal").contains("leftIND")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            left = jsonObject.getString("data");
                            leftstr.setText(left);
                        } else if (jsonObject.getString("signal").contains("hazard")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            hazard = jsonObject.getString("data");
                            hazardstr.setText(hazard);
                        } else if (jsonObject.getString("signal").contains("vehicle_err")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            error = jsonObject.getString("data");
                            errorstr.setText(error);
                        } else if (jsonObject.getString("signal").contains("regen")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            regen = jsonObject.getString("data");
                            regenstr.setText(regen);
                        } else if (jsonObject.getString("signal").contains("abs")) {
                            //Log.d("speeedddd", jsonObject.getString("distance"));
                            abs = jsonObject.getString("data");
                            abs_str.setText(abs);
                        }


                       /* switch (intent.getStringExtra("signal")){
                            case "speed":
                                if(intent.getStringExtra("signal").equalsIgnoreCase("speed")) {
                                    Log.d("speeedddd", intent.getStringExtra("signal") + "\t" + intent.getStringExtra("timestamp"));
                                    speed = intent.getStringExtra("data");
                                    txtspeed.setText(speed);
                                }
                                break;
                            case "distance":
                                if(intent.getStringExtra("signal").equalsIgnoreCase("distance")) {
                                    Log.d("speeedddd", intent.getStringExtra("signal") + "\t" + intent.getStringExtra("timestamp"));
                                    distance = intent.getStringExtra("data");
                                    txtdistance.setText(distance);
                                }
                                break;
                            case "odometer":
                                Log.d("speeedddd", intent.getStringExtra("signal")+"\t"+intent.getStringExtra("timestamp"));
                                odo=intent.getStringExtra("data");
                                txtodo.setText(odo);
                                break;
                        }*/


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /*if(intent.getStringExtra("speed")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("speed"));
                        speed=intent.getStringExtra("speed");
                        txtspeed.setText(speed);
                    }
                    else if(intent.getStringExtra("distance")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("distance"));
                        distance=intent.getStringExtra("distance");
                        txtdistance.setText(distance);
                    }
                    else if(intent.getStringExtra("odometer")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("odometer"));
                        odo=intent.getStringExtra("odometer");
                        txtodo.setText(odo);
                    }else if(intent.getStringExtra("vhlchrg")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("vhlchrg"));
                        vehiclechrg=intent.getStringExtra("vhlchrg");
                        txtvehiclechrg.setText(vehiclechrg);
                    }else if(intent.getStringExtra("chrgtym")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("chrgtym"));
                        chrgtym=intent.getStringExtra("chrgtym");
                        txtchrgtym.setText(chrgtym);
                    }else if(intent.getStringExtra("soc")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("soc"));
                        soc=intent.getStringExtra("soc");
                        txtsoc.setText(soc);
                    }else if(intent.getStringExtra("lowSOC")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("lowSOC"));
                        LowSoc=intent.getStringExtra("lowSOC");
                        txtLowSoc.setText(LowSoc);
                    }else if(intent.getStringExtra("btrySOH")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("btrySOH"));
                        batterysoh=intent.getStringExtra("btrySOH");
                        txtbatterysoh.setText(batterysoh);
                    }else if(intent.getStringExtra("rightIND")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("rightIND"));
                        right=intent.getStringExtra("rightIND");
                        rightstr.setText(right);
                    }else if(intent.getStringExtra("leftIND")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("leftIND"));
                        left=intent.getStringExtra("leftIND");
                        leftstr.setText(left);
                    }else if(intent.getStringExtra("hzrd")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("hzrd"));
                        hazard=intent.getStringExtra("hzrd");
                        hazardstr.setText(hazard);
                    }else if(intent.getStringExtra("error")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("error"));
                        error=intent.getStringExtra("error");
                        errorstr.setText(error);
                    }else if(intent.getStringExtra("regen")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("regen"));
                        regen=intent.getStringExtra("regen");
                        regenstr.setText(regen);
                    }else if(intent.getStringExtra("abs")!=null) {
                        Log.d("speeedddd", intent.getStringExtra("abs"));
                        abs=intent.getStringExtra("abs");
                        abs_str.setText(abs);
                    }*/






                    /*try {
                        JSONObject jsonObject = new JSONObject(message);
                        txtspeed.setText(jsonObject.getString("speed"));
                        txtdistance.setText(jsonObject.getString("distance"));
                        txtodo.setText(jsonObject.getString("odometer"));
                        txtvehiclechrg.setText(jsonObject.getString("vhlchrg"));
                        txtchrgtym.setText(jsonObject.getString("chrgtym"));
                        txtsoc.setText(jsonObject.getString("soc"));
                        txtLowSoc.setText(jsonObject.getString("lowSOC"));
                        txtbatterysoh.setText(jsonObject.getString("btrySOH"));
                        rightstr.setText(jsonObject.getString("rightIND"));
                        leftstr.setText(jsonObject.getString("leftIND"));
                        hazardstr.setText(jsonObject.getString("hzrd"));
                        errorstr.setText(jsonObject.getString("error"));
                        regenstr.setText(jsonObject.getString("regen"));
                        abs_str.setText(jsonObject.getString("abs"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                }
            }
        }
    }
}