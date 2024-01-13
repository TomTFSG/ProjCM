#include <Wire.h> 
#include <LiquidCrystal_I2C.h>
#include <DHTesp.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>


String ID_MQTT;
char *letters = "abcdefghijklmnopqrstuvwxyz0123456789";
#define TOPIC_PUBLISH_TEMPERATURE "temperature"
#define TOPIC_PUBLISH_HUMIDITY "humidity"
#define TOPIC_PUBLISH_LIGHT "light"
#define TOPIC_REGA "water"
#define PUBLISH_DELAY 500
unsigned long publishUpdate;
const size_t bufferSize = JSON_OBJECT_SIZE(4) + 50;

// Wi-Fi settinges
const char *SSID = "Wokwi-GUEST";
const char *PASSWORD = "";
String msgR="nope";
String abacate="A";
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


  if (String(topic) == TOPIC_REGA) {
    // RECEBE MENSAGEM AQUI/////////////////////////////////////////////////////////////
    msgR="";
    for (int i = 0; i < length; i++) {
    char c = (char)payload[i];
    msgR += c;
    }
    DynamicJsonDocument doc(bufferSize);
    DeserializationError error = deserializeJson(doc, msgR);
    if (error) {
    Serial.print(F("deserializeJson() failed: "));
    Serial.println(error.c_str());
    return;
  }
  // Extract variables from the JSON document
  double temperature = doc["temp"];
  double humidity = doc["humi"];
  double light = doc["light"];
  double type=doc["type"];
  regar(type,temperature,humidity,light);
  }
  
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
      MQTT.subscribe(TOPIC_REGA);
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
    
LCD.setCursor(9,1);
LCD.print(abacate);

MQTT.loop();
delay(500); 
}
void regar(double type, double temp, double humi, double light){
  double fatorLuz=fatLuz(light);
  double fatorTemp=fatTemp(temp);
  double fatorHumi=fatHumi(humi,type);
  double water;
  if(type==0){
    water=200;
  }
  else if(type==1){
    water=300;
  }
  else{
    water=400;
  }
  double rega=(fatorLuz*0.33+fatorTemp*0.33+fatorHumi*0.34)*water;
  abacate=rega;
  //MQTT.publish(TOPIC_PUBLISH_REGA,String(rega).c_str());
}
double fatLuz(double a) {
    // Define the minimum and maximum values for a
    double minA = 0;
    double maxA = 100000;

    // Define the minimum and maximum values for b
    double minB = 0.5;
    double maxB = 1.2;

    // Map the value of a to the range [0, 1]
    double normalizedA = (a - minA) / (maxA - minA);

    // Map the normalized value to the range [minB, maxB]
    double b = minB + normalizedA * (maxB - minB);

    // Ensure that the result is within the specified range
    return constrain(b, minB, maxB);
}

double fatTemp(double a) {
    // Define the minimum and maximum values for a
    double minA = 10;
    double maxA = 40;

    // Define the minimum and maximum values for b
    double minB = 0.7;
    double maxB = 1.2;

    // Map the value of a to the range [0, 1]
    double normalizedA = (a - minA) / (maxA - minA);

    // Map the normalized value to the range [minB, maxB]
    double b = minB + normalizedA * (maxB - minB);

    // Ensure that the result is within the specified range
    return constrain(b, minB, maxB);
}

double fatHumi(double a,double t) {
    // Define the minimum and maximum values for a
    double minA = 10;
    double maxA = 45;

    // Define the minimum and maximum values for b
    double minB, maxB;

    if (t == 0) {
        minB = 1;
        maxB = 0.1;
    } else if (t == 1) {
        minB = 1.5;
        maxB = 0.5;
    } else {
        minB = 2;
        maxB = 0.7;
    }

    // Map the value of a to the range [0, 1]
    double normalizedA = (a - minA) / (maxA - minA);

    // Map the normalized value to the range [minB, maxB]
    double b = minB + normalizedA * (maxB - minB);

    // Ensure that the result is within the specified range
    return constrain(b, minB, maxB);

}