package com.example.tiarh.sendinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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


public class MainActivity extends AppCompatActivity {

        protected static TextView txtBattery;
        private TextView txtTemperature;

        public static int level;

    static MqttAndroidClient client;

    String topicStr = "temperatura/esp";

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            txtBattery = (TextView) findViewById(R.id.batterymeter_txt);
            txtTemperature = (TextView) findViewById(R.id.temperature_txt);
            batteryLevel();

            String clientId = MqttClient.generateClientId();
            client = new MqttAndroidClient(this.getApplicationContext(), "tcp://192.168.2.2:1883", clientId);

            MqttConnectOptions options = new MqttConnectOptions();

            try {
                IMqttToken token = client.connect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Toast.makeText(MainActivity.this, "Connesso a DGGM Domotica !!", Toast.LENGTH_LONG).show();
                        setSubscription();
                        String topic = "controllo/esp";
                        String message = "15";
                        OFF();
                        try {
                            client.publish(topic, message.getBytes(), 0, false);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Toast.makeText(MainActivity.this, "Connessione con DGGM Domotica fallita!!", Toast.LENGTH_LONG).show();

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Toast.makeText(MainActivity.this, "Connessione persa", Toast.LENGTH_LONG).show();
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

    public void OFF(){
        String topic = "temperatura/esp";
        String message = "" + level;
        try {
            client.publish(topic, message.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

        //IMPOSTO MENU OPZIONI
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.impostazioni_app, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch(item.getItemId()) {
                case R.id.mqtt:
                    Intent mqtt = new Intent(MainActivity.this,OpzioniMqtt.class);
                    startActivity(mqtt);
                    return true;
                case R.id.http:
                    Intent http = new Intent(MainActivity.this, OpzioniHttp.class);
                    startActivity(http);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

    //LIVELLO BATTERIA
    public void batteryLevel() {
        IntentFilter batteryLevelFliter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryLevelReciver, batteryLevelFliter);
    }

    public BroadcastReceiver mBatteryLevelReciver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            level = -1;
            if (rawLevel >= 0 && scale > 0) {
                level = (rawLevel * 100) / scale;
            }
            MainActivity.txtBattery.setText("Livello Batteria Rimanente:  " + level + "%");
        }

    };

}
