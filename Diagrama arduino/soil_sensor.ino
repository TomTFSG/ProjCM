#include <Wire.h> 
#include <LiquidCrystal_I2C.h>
#include <DHTesp.h>
#include <WiFi.h>
#include <PubSubClient.h>

String ID_MQTT;
char *letters = "abcdefghijklmnopqrstuvwxyz0123456789";
#define TOPIC_PUBLISH_TEMPERATURE "temperature"
#define TOPIC_PUBLISH_HUMIDITY "humidity"
#define TOPIC_PUBLISH_LIGHT "light"
#define PUBLISH_DELAY 500
unsigned long publishUpdate;

// Wi-Fi settinges
const char *SSID = "Wokwi-GUEST";
const char *PASSWORD = "";

// Define MQTT Broker and PORT
const char *BROKER_MQTT = "broker.hivemq.com";
int BROKER_PORT = 1883;
WiFiClient espClient;
PubSubClient MQTT(espClient); 


LiquidCrystal_I2C LCD(0x27,16,2);
const int LDR_PIN = 36;
const int DHT_PIN = 15;
// LDR Characteristics
const float GAMMA = 0.7;
const float RL10 = 85;
DHTesp dhtSensor;

void startWifi(void);
void initMQTT(void);
void callbackMQTT(char *topic, byte *payload, unsigned int length);
void reconnectMQTT(void);
void reconnectWiFi(void);
void checkWiFIAndMQTT(void);

void startWifi(void) {
  reconnectWiFi();
}

// Starts everything from MQTT
void initMQTT(void) {
  MQTT.setServer(BROKER_MQTT, BROKER_PORT);
  MQTT.setCallback(callbackMQTT);
}

//RECEBER MENSAGENS
void callbackMQTT(char *topic, byte *payload, unsigned int length) {
  String msg;

  // Convert payload to string
  for (int i = 0; i < length; i++) {
    char c = (char)payload[i];
    msg += c;
  }

  Serial.printf("Topic: %s\n", topic);
  Serial.printf("Message: %s\n", msg, topic);


  /*if (String(topic) == TOPIC_LIGHT) {
    // RECEBE MENSAGEM AQUI
    if (msg.equals("bingbong")) {


    }
  }
  */
}

// SUBSCREVER A TOPICOS
void reconnectMQTT(void) {
  while (!MQTT.connected()) {
    ID_MQTT = "";
    Serial.print("* Starting connection with broker: ");
    Serial.println(BROKER_MQTT);

    int i = 0;
    for (i = 0; i < 10; i++) {
      ID_MQTT = ID_MQTT + letters[random(0, 36)];
    }

    if (MQTT.connect(ID_MQTT.c_str())) {
      Serial.print("* Connected to broker successfully with ID: ");
      Serial.println(ID_MQTT);
      //MQTT.subscribe(TOPIC_LIGHT);
    } else {
      Serial.println("* Failed to connected to broker. Trying again in 2 seconds.");
      delay(2000);
    }
  }
}

// N Mexer nisto
void checkWiFIAndMQTT(void) {
  if (!MQTT.connected())
    reconnectMQTT();
  reconnectWiFi();
}
void reconnectWiFi(void) {
  if (WiFi.status() == WL_CONNECTED)
    return;

  WiFi.begin(SSID, PASSWORD); // Conecta na rede WI-FI

  Serial.print("* Connecting to Wifi ");
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }
  
  Serial.println("");
  Serial.print("* Successfully connected to Wi-Fi, with local IP: ");
  Serial.println(WiFi.localIP());

  LCD.clear();
  LCD.setCursor(0, 0);
  LCD.print("Finished!");
  LCD.setCursor(0, 1);
  LCD.print("-- ");
  LCD.print(WiFi.localIP());
}

void setup()
{
  Wire.begin(23, 22);
  Serial.begin(115200);

  pinMode(LDR_PIN, INPUT);
  dhtSensor.setup(DHT_PIN, DHTesp::DHT22);
  LCD.init();
  LCD.backlight();
  startWifi();
  initMQTT();
}

void loop(){
checkWiFIAndMQTT();

//HUMIDADE DA TERRA
int16_t i = analogRead(34);
MQTT.publish("humidity",String(i).c_str());
String msg;
if (i < 10) {
  msg = "DRY";
} else if (i > 45) {
  msg = "WET";
}else if (i > 65) {
  msg = "DRENCHED";
} else {
  msg = "OK";
}
LCD.clear();
LCD.print("S:");
LCD.print(msg);

//LUZ
int analogValue = analogRead(LDR_PIN);
float voltage = (analogValue/4) / 1024. * 3.3;
float resistance = 5000 * voltage / (1 - voltage / 3.3);
float lux = pow(RL10 * 1e3 * pow(10, GAMMA) / resistance, (1 / GAMMA));
LCD.setCursor(6,0);
LCD.print("L:");
LCD.print(lux);
MQTT.publish(TOPIC_PUBLISH_LIGHT,String(lux).c_str());

//TEMPERATURA E HUMIDADE DO AR
TempAndHumidity  data = dhtSensor.getTempAndHumidity();
LCD.setCursor(0, 1);
LCD.print("T:" + String(data.temperature, 2) + "C");
MQTT.publish(TOPIC_PUBLISH_TEMPERATURE, String(data.temperature, 2).c_str());
    


MQTT.loop();
delay(500); 
}
