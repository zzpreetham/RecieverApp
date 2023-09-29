package com.royalenfield.recieverapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.royalenfield.recieverapp.R;
import com.royalenfield.recieverapp.activity.MainActivity;
import com.royalenfield.recieverapp.database.MqttDBHelper;
import com.royalenfield.recieverapp.model.MqttDataModel;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.Objects;

public class PendingDataUpload extends AsyncTask<Void,Void,Void> {

    MqttDBHelper mqttDBHelper;
    Context context;
    //MQTT server Creditentials
    String broker = "tcp://35.200.186.3:1883";
    //String broker = "tcp://0.0.0.0:1883";
    String topic = "gprsData";
    String username = "royalenfield";
    String password = "RoyalEnfieldMqttBroker";
    String clientid = "publish_client";
    String content = "Hello MQTT";
    int qos = 0;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public PendingDataUpload(MqttDBHelper mqttDBHelper,Context context) {
        this.mqttDBHelper = mqttDBHelper;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        ArrayList<MqttDataModel>mqttDataModelArrayList = mqttDBHelper.readMqttData();
        for(int i=0;i<mqttDataModelArrayList.size();i++){
            try {
                content = mqttDataModelArrayList.get(i).getRaw_data();
                    try {
                        MqttClient client = new MqttClient(broker, clientid, new MemoryPersistence());
                        MqttConnectOptions options = new MqttConnectOptions();
                        options.setUserName(username);
                        options.setPassword(password.toCharArray());
                        options.setConnectionTimeout(30);
                        options.setKeepAliveInterval(60);
                        options.setAutomaticReconnect(false);
                        //options.setExecutorServiceTimeout();
                        // connect
                        Log.d("mqtt","attemting to connect "+System.currentTimeMillis());
                        client.connect(options);
                        Log.d("mqtt","connected"+System.currentTimeMillis());
                        // create message and setup QoS
                        MqttMessage message = new MqttMessage(content.getBytes());
                        message.setQos(qos);
                        // publish message
                        client.publish(topic, message);
                        System.out.println("Message published");
                        System.out.println("topic: " + topic);
                        System.out.println("message content: " + content);
                        // disconnect
                        Log.d("Pending_mqtt","Disconnected");
                        client.disconnect();
                        // close client
                        client.close();
                    }
                    catch (MqttException e){
                        e.printStackTrace();
                        Log.d("error",e.getMessage());
                    }
                    mqttDBHelper.updateCourse(mqttDataModelArrayList.get(i).getId_col());

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }
}
