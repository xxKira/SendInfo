package com.example.tiarh.sendinfo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by tiarh on 05/09/2018.
 */

public class Mqtt extends MainActivity {

    MqttAndroidClient client;

    String topicStr = "temperatura/esp";

    public void mqtt() {

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://192.168.2.2:1883", clientId);

        MqttConnectOptions options = new MqttConnectOptions();

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(Mqtt.this, "Connesso a DGGM Domotica !!", Toast.LENGTH_LONG).show();
                    setSubscription();
                    String topic = "controllo/esp";
                    String message = "15";
                    try {
                        client.publish(topic, message.getBytes(), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(Mqtt.this, "Connessione con DGGM Domotica fallita!!", Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String ciao = new String(message.getPayload());

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

        });
    }

    private void setSubscription() {
        try{
            client.subscribe(topicStr, 0);
        }catch (MqttException e){
            e.printStackTrace();
        }
    }
}
