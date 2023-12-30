package com.example.projeto;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    SharedViewModel sharedViewModel;

    MqttHelper helper;
    String brookerUri;
    String clientId;
    Double temperatura;
    Double humidade;
    Double luz;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        setContentView(R.layout.activity_main);
        brookerUri="tcp://broker.hivemq.com:1883";
        clientId="userAndroid";
        helper =new MqttHelper(getApplicationContext(),brookerUri,clientId);
        Log.w(TAG,"HELLO");

        helper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                helper.subscribe("temperature");
                Log.i(TAG,"SUBSCRIBED");
                helper.subscribe("humidity");
                helper.subscribe("light");
                helper.subscribe("spider");
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
                Log.d(TAG,"T: "+topic+ " P: "+payload);

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