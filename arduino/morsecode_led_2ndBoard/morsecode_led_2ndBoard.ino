#include <WiFiNINA.h>
#include <SPI.h>
#include <ArduinoJson.h>
#include <ArduinoHttpClient.h>
#include <LiquidCrystal.h>

const int rs = 11,
          en = 12,
          d4 = 2,
          d5 = 3,
          d6 = 4,
          d7 = 5;

const int knopf = 9;
int buzzerPin = 10;

LiquidCrystal lcd(rs, en, d4, d5, d6, d7);

char ssid[] = "A34 von Tristan";
char password[] = "ehrsfdevtu4cqzp";

int status = WL_IDLE_STATUS;
WiFiClient wifi;
HttpClient client(wifi, "172.20.10.8", 8080);
const int plantId = 1;

const int dotTime = 200;
const int dashTime = 600;

const char* morseDigits[10] = {
  "-----", // 0
  ".----", // 1
  "..---", // 2
  "...--", // 3
  "....-", // 4
  ".....", // 5
  "-....", // 6
  "--...", // 7
  "---..", // 8
  "----."  // 9
};


void setRGB(String color) {

  // eingebaute RGB-LED
  digitalWrite(LEDR, LOW);
  digitalWrite(LEDG, LOW);
  digitalWrite(LEDB, LOW);

  if (color == "red") {
    digitalWrite(LEDR, HIGH);
  } else if (color == "green") {
    digitalWrite(LEDG, HIGH);
  } else if (color == "blue") {
    digitalWrite(LEDB, HIGH);
  }
}

void HeartBeat() {
  tone(buzzerPin, 31, 200);
}

void setup() {
  // put your setup code here, to run once:

  Serial.begin(115200);

  analogWrite(A3, 0);

  lcd.begin(16, 2);
  lcd.setCursor(0, 1);
  lcd.clear();
  lcd.print("hiiii");

  pinMode(8, OUTPUT);
  pinMode(7, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(5, OUTPUT);

  pinMode(9, INPUT_PULLUP);


  //pinMode(LEDR, OUTPUT);
  //pinMode(LEDG, OUTPUT);
  //pinMode(LEDB, OUTPUT);

  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    while (true)
      ;
  }

  String fv = WiFi.firmwareVersion();
  if (fv < "1.0.0") {
    Serial.println("Please upgrade the firmware");
  }

  while (status != WL_CONNECTED) {
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);

    status = WiFi.begin(ssid, password);
    delay(10000);
  }

  Serial.println("Connected to wifi");
  printWiFiStatus();
}

void loop() {

  //Serial.println(digitalRead(9));

  if (digitalRead(9) == LOW) {

    //Serial.println("if");
    
    digitalWrite(5,1);
    String path = "/api/v1/plants/" + String(plantId) + "/measurements/latest";
    client.get(path);

    int statusCode = client.responseStatusCode();
    String response = client.responseBody();

    Serial.print("Status code: ");
    Serial.println(statusCode);
    Serial.print("Response: ");
    Serial.println(response);

    if (statusCode == 200) {
      Serial.println(response);

      JsonDocument doc;

      DeserializationError error =
        deserializeJson(doc, response);

      if (!error) {

        int temperature = doc["temperature"] | 0;
        int soilMoisture = doc["soilMoisture"] | 0;
        int airMoisture = doc["airMoisture"] | 0;

        Serial.println(temperature);
        Serial.println(soilMoisture);
        Serial.println(airMoisture);

        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("T:");
        lcd.print(temperature);
        lcd.print("C S:");
        lcd.print(soilMoisture);
        lcd.print("%");
        lcd.setCursor(0, 1);
        lcd.print("Air:");
        lcd.print(airMoisture);
        lcd.print("%");

        changeLight(temperature);
        buzz(soilMoisture);

        digitalWrite(6, 0);
        digitalWrite(8, 0);
        digitalWrite(7, 0);
       
      }
    }
  }
}

void printWiFiStatus() {
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}

void changeLight(int temp) {
   digitalWrite(5,0);
  if (temp > 30) {
    digitalWrite(6, 0);
    digitalWrite(8, 1);
    digitalWrite(7, 0);
  } else if (temp < 10) {
    digitalWrite(6, 0);
    digitalWrite(8, 1);
    digitalWrite(7, 1);
  }else{
    digitalWrite(6, 0);
    digitalWrite(8, 0);
    digitalWrite(7, 1);
  }
}

void buzz(int moisture) {
  char buffer[8];
  itoa(moisture, buffer, 10);

  for (int i = 0; buffer[i] != '\0'; i++) {
    char digit = buffer[i];

    if (digit >= '0' && digit <= '9') {
      const char* code = morseDigits[digit - '0'];

      for (int j = 0; code[j] != '\0'; j++) {
        buzzMorseChar(code[j]);
      }

      delay(3 * dotTime); // gap between digits
    }
  }
}

void beep(int duration) {
  tone(buzzerPin, 1000);
  delay(duration);
  noTone(buzzerPin);
  delay(dotTime); // gap between symbols
}

void buzzMorseChar(char c) {
  if (c == '.') {
    beep(dotTime);
  } else if (c == '-') {
    beep(dashTime);
  }
}
