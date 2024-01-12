package com.example.projeto.misc;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String CHANNEL_ID = "Regador9000";




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
                    Log.d("AHAHAHAHAAHA", "T: " + topic + " P: " + payload);

                    if (topic.equals("temperature")) {
                        temperatura = valor;
                    } else if (topic.equals("humidity")) {
                        humidade = valor;
                    } else if (topic.equals("light")) {
                        luz = valor;
                        publishMqttMessage();
                    } else if(topic.equals("rega") && payload != null){
                        Log.w("REGADO",payload+"ml regados");
                        /*
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setSmallIcon(R.drawable.logo2)
                                .setContentTitle("REGADOR 9000")
                                .setContentText("Regado com "+payload+"ml de água")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        */
                        //stopSelf();
                    }
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
}