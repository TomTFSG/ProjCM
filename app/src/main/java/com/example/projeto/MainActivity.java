package com.example.projeto;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.example.projeto.fragments.Menu;
import com.example.projeto.misc.AlarmReceiver;
import com.example.projeto.misc.FeedReaderDbHelper;
import com.example.projeto.misc.MqttHelper;
import com.example.projeto.viewmodels.SharedViewModel;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public SharedViewModel sharedViewModel;
    FeedReaderDbHelper dbHelper;
    SQLiteDatabase db;
    MqttHelper helper;
    String brookerUri = "tcp://broker.hivemq.com:1883";
    String clientId = "userAndroid";
    Double temperatura;
    Double humidade;
    Double luz;
    public AlarmManager alarmManager;
    public PendingIntent pendingIntent;

    int hora;
    int minutos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        helper = new MqttHelper(getApplicationContext(), brookerUri, clientId);
        Log.w(TAG,"HELLO");
        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase(); // Use getWritableDatabase() instead of getReadableDatabase()

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);



        ContentValues values = new ContentValues();
        values.put(FeedReaderDbHelper.COLUMN_NAME_ATUAL, 1); // Use an appropriate integer value
        values.put(FeedReaderDbHelper.COLUMN_NAME_HORAS, 17);
        values.put(FeedReaderDbHelper.COLUMN_NAME_MINUTOS, 55);
        long newRowId = db.insertWithOnConflict(
                FeedReaderDbHelper.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
        if (newRowId != -1) {
            Log.i(TAG, "Value inserted successfully with ID: " + newRowId);
        } else {
            Log.e(TAG, "Error inserting value into the database");
        }


        ////////////////////////////////////////////////////////
        //ALARME PARA REGAR AS PLANTAS
        Context context = getApplicationContext();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);


        ////////////////////////////////////////////////////////
        //HORAS E MINUTOS
        hora = dbHelper.getValueFromColumnName(FeedReaderDbHelper.COLUMN_NAME_HORAS);
        minutos = dbHelper.getValueFromColumnName(FeedReaderDbHelper.COLUMN_NAME_MINUTOS);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minutos);
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
                helper.subscribe("rega");
                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame, new Menu(),null)
                            .commit();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {}

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload=new String(message.getPayload());
                double valor = Double.parseDouble(payload);
                //Log.d(TAG,"T: "+topic+ " P: "+payload);

                ContentValues values = new ContentValues();
                String key = "";


                if (topic.equals("temperature")) {
                    key = FeedReaderDbHelper.COLUMN_NAME_TEMP;
                    sharedViewModel.setTemperatura(valor);
                    temperatura = valor;
                } else if (topic.equals("humidity")) {
                    key = FeedReaderDbHelper.COLUMN_NAME_HUMI;
                    sharedViewModel.setHumidade(valor);
                    humidade = valor;
                } else if (topic.equals("light")) {
                    key = FeedReaderDbHelper.COLUMN_NAME_LIGHT;
                    final double maxLUX = Math.pow(10, 5);
                    // escala logaritmica
                    final double valEscalada = 100 * (Math.log(valor + 0.9) / Math.log(maxLUX + 0.9));
                    sharedViewModel.setLuz(Math.round(valEscalada * 10) / 10.0);
                    luz = valor;
                }

                values.put(FeedReaderDbHelper.COLUMN_NAME_TIME, getCurrentTime());
                values.put(key, Double.toString(valor));
                db.insert(FeedReaderDbHelper.TABLE_NAME, null, values);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
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

    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}