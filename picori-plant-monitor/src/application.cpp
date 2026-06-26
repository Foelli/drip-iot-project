#include "application.h"
#include <Arduino.h>

void Firmware::init() {
  Serial.begin(9600);
  statusLED.init();
  moistureSensor.init();
  airSensor.init();
  Serial.println("Firmware started");
}

void Firmware::idle() {
  statusLED.toggle();

  SensorReading reading;
  reading.soilMoisture = moistureSensor.readMoisturePercent();
  reading.temperatureC = airSensor.readTemperature();
  reading.airHumidity = airSensor.readHumidity();

  Serial.println("Sensor reading");
  Serial.println("--------------");
  Serial.print("Soil: ");
  Serial.print(reading.soilMoisture);
  Serial.println(" %");
  Serial.print("Temp: ");
  Serial.print(reading.temperatureC, 1);
  Serial.println(" C");
  Serial.print("Air:  ");
  Serial.print(reading.airHumidity, 1);
  Serial.println(" %");
  Serial.println();

  delay(2000);
}
