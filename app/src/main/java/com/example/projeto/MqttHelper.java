package com.example.projeto;

import android.content.Context;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper {
    private static final int DEFAUL_QOS=1;
    private MqttAndroidClient mqttAndroidClient;

    public MqttHelper(Context context, String brokerUri, String clientId) {
        mqttAndroidClient = new MqttAndroidClient(context, brokerUri, clientId);
    }

    public void setCallback(MqttCallback callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public void connect(MqttConnectOptions options) {
        try {
            mqttAndroidClient.connect(options, null, null);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        try {
            mqttAndroidClient.subscribe(topic,DEFAUL_QOS, null);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(DEFAUL_QOS);
            message.setRetained(false);
            mqttAndroidClient.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
