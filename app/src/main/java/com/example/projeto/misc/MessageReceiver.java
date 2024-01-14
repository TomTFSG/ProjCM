package com.example.projeto.misc;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.projeto.MainActivity;
import com.example.projeto.R;
import com.example.projeto.viewmodels.SharedViewModel;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MessageReceiver extends IntentService {
    private static final String TAG = "MessageReceiver";
    public static final String ACTION_UPDATE_TYPE = "com.example.projeto.ACTION_UPDATE_TYPE";

    MqttHelper helper;
    Double temperatura;
    Double humidade;
    Double luz;
    Double type;
    FeedReaderDbHelper dbHelper;
    SQLiteDatabase db;
    SharedViewModel sharedViewModel;

    public MessageReceiver() {
        super("MessageReceiver");
    }
    @Override
    public void onCreate() {
        super.onCreate();

        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                FeedReaderDbHelper.COLUMN_NAME_ATUAL
        };

        Cursor cursor = db.query(
                FeedReaderDbHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(FeedReaderDbHelper.COLUMN_NAME_ATUAL);
            type = cursor.getDouble(columnIndex);
            cursor.close();
        } else {
            Log.e(TAG, "Error reading value from the database");
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        acquireWakeLock();
        initMqttHelper();
        connectToMqttBroker();

        disconnectFromMqttBroker();
        releaseWakeLock();
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp:WakeLockTag"
        );
        wakeLock.acquire();
    }

    private void releaseWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp:WakeLockTag"
        );
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void initMqttHelper() {
        try {
            String brokerUri = "tcp://broker.hivemq.com:1883";
            String clientId = "userAndroid";
            helper = new MqttHelper(getApplicationContext(), brokerUri, clientId);
            helper.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    Log.d(TAG, "Connected to MQTT broker");
                    helper.subscribe("temperature");
                    helper.subscribe("humidity");
                    helper.subscribe("light");
                    helper.subscribe("rega");
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Connection to MQTT broker lost");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    double valor = Double.parseDouble(payload);
                    Log.d("ding", "T: " + topic + " P: " + payload);
                    ContentValues values = new ContentValues();
                    String key = "";
                    if (topic.equals("temperature")) {
                        key = FeedReaderDbHelper.COLUMN_NAME_TEMP;
                        temperatura = valor;
                    } else if (topic.equals("humidity")) {
                        key = FeedReaderDbHelper.COLUMN_NAME_HUMI;
                        humidade = valor;
                    } else if (topic.equals("light")) {
                        key = FeedReaderDbHelper.COLUMN_NAME_LIGHT;
                        luz = valor;
                        publishMqttMessage();
                    } else if(topic.equals("rega") && payload != null){
                        key = FeedReaderDbHelper.COLUMN_NAME_REGA;
                        Log.w("REGADO",payload+"dl regados");
                        disconnectFromMqttBroker();
                        sendNotification(payload);
                        releaseWakeLock();

                        AlarmManager alarmManager;
                        PendingIntent pendingIntent;
                        int hora;
                        int minutos;
                        Context context = getApplicationContext();
                        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        hora = dbHelper.getValueFromColumnName(FeedReaderDbHelper.COLUMN_NAME_HORAS);
                        minutos = dbHelper.getValueFromColumnName(FeedReaderDbHelper.COLUMN_NAME_MINUTOS);
                        boolean isAlarmSet = (PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT) != null);

                        if (!isAlarmSet) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            calendar.set(Calendar.HOUR_OF_DAY, hora);
                            calendar.set(Calendar.MINUTE, minutos);

                            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                                calendar.add(Calendar.DAY_OF_MONTH, 1);
                            }

                            long triggerTime = calendar.getTimeInMillis();
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                        }
                    }

                    values.put(FeedReaderDbHelper.COLUMN_NAME_TIME, getCurrentTime());
                    values.put(key, Double.toString(valor));
                    db.insert(FeedReaderDbHelper.TABLE_NAME, null, values);

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Message delivery complete
                }
            });
        }
        catch (Exception e) {
            Log.e(TAG, "Error initializing MqttHelper", e);
        }
    }

    private void connectToMqttBroker() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        helper.connect(mqttConnectOptions);
    }

    private void disconnectFromMqttBroker() {
        try {
            if (helper != null) {
                helper.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting from MQTT broker", e);
        }
    }
    private void publishMqttMessage(){
        Log.d("TRIED","tp: "+temperatura +" h:"+humidade+" l:"+luz+" ty:"+type);
        if (temperatura != null && humidade != null && luz != null) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("type", type.toString());
                payload.put("temp", temperatura.toString());
                payload.put("humi", humidade.toString());
                payload.put("light", luz.toString());
                String jsonS = payload.toString();
                helper.publish("water", jsonS);
                Log.d("PUBLICADOR","PUBLISHED "+jsonS );
            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON payload", e);
            }
        }
    }
    private void sendNotification(String m){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "rega_notif";
        String channelName = "Regador 2024";
        int notificationId = 1;

        // Check if the device is running Android Oreo or higher, and create a notification channel if needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Create an Intent to launch when the notification is tapped.
        Intent intent = new Intent(this, MainActivity.class);  // Replace YourActivity with the appropriate activity.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo_notif)
                .setContentTitle("Regador 9000")
                .setContentText("Your plant was been watered with "+m+"ml of water.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Show the notification.
        notificationManager.notify(notificationId, notificationBuilder.build());

    }
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}