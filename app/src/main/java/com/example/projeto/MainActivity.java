package com.example.projeto;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    SharedViewModel sharedViewModel;
    FeedReaderDbHelper dbHelper;
    SQLiteDatabase db;
    MqttHelper helper;
    String brookerUri;
    String clientId;
    Double temperatura;
    Double humidade;
    Double luz;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase(); // Use getWritableDatabase() instead of getReadableDatabase()
        ContentValues values = new ContentValues();
        values.put(FeedReaderDbHelper.COLUMN_NAME_ATUAL, 1); // Use an appropriate integer value
        long newRowId = db.replace(FeedReaderDbHelper.TABLE_NAME, null, values);
        if (newRowId != -1) {
            Log.i(TAG, "Value inserted successfully with ID: " + newRowId);
        } else {
            Log.e(TAG, "Error inserting value into the database");
        }


        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        setContentView(R.layout.activity_main);
        brookerUri="tcp://broker.hivemq.com:1883";
        clientId="userAndroid";
        helper =new MqttHelper(getApplicationContext(),brookerUri,clientId);
        Log.w(TAG,"HELLO");

        //ALARME PARA REGAR AS PLANTAS
        Context context=getApplicationContext();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);


        //horas
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 7);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        long triggerTime = calendar.getTimeInMillis();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);

        helper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                helper.subscribe("temperature");
                Log.i(TAG,"SUBSCRIBED");
                helper.subscribe("humidity");
                helper.subscribe("light");
                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame, new Menu(),null)
                            .commit();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload=new String(message.getPayload());
                double valor=Double.parseDouble(payload);
                //Log.d(TAG,"T: "+topic+ " P: "+payload);

                if (topic.equals("temperature")) {
                    sharedViewModel.setTemperatura(valor);
                    temperatura=valor;
                }
                else if (topic.equals("humidity")) {
                    sharedViewModel.setHumidade(valor);
                    humidade=valor;
                }
                else if(topic.equals("light")){
                    sharedViewModel.setLuz(valor);
                    luz=valor;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        /*
        try {
            helper.publish("light", "bingbong");
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        helper.connect(mqttConnectOptions);
        Log.i(TAG,"ABACATE");


    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}