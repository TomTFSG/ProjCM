package com.example.projeto.misc;
import android.content.Context;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper {
    private static final int DEFAULT_QOS = 1;

    private MqttAndroidClient mqttAndroidClient;

    public MqttHelper(Context context, String brokerUri, String clientId) {
        mqttAndroidClient = new MqttAndroidClient(context, brokerUri, clientId);
    }

    public void setCallback(MqttCallback callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public void connect(MqttConnectOptions options) throws MqttException {
        mqttAndroidClient.connect(options, null, null);
    }

    public void disconnect() throws MqttException {
        mqttAndroidClient.disconnect();
    }

    public void subscribe(String topic) throws MqttException {
        // Add the receiver permission when subscribing
        String receiverPermission = "com.example.projeto.permission.MQTT_RECEIVER";
        mqttAndroidClient.subscribe(topic, DEFAULT_QOS, null);
    }

    public void publish(String topic, String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(DEFAULT_QOS);
        message.setRetained(false);
        mqttAndroidClient.publish(topic, message);
    }
}
