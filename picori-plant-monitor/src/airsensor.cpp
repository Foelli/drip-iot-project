#include "airsensor.h"
#include <Arduino.h>

#define AIR_SENSOR_PIN 16
#define AIR_SENSOR_TYPE DHT22

AirSensor::AirSensor() : dht(AIR_SENSOR_PIN, AIR_SENSOR_TYPE) {}

void AirSensor::init() {
  dht.begin();
  Serial.println("Air sensor initialized");
}

void AirSensor::handleEvent(Event event) {}

void AirSensor::readAirValues() {
  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();

  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("Air sensor read failed");
    Serial.println();
    return;
  }

  Serial.println("Air sensor");
  Serial.println("----------");
  Serial.print("Temperature: ");
  Serial.print(temperature, 1);
  Serial.println(" C");
  Serial.print("Humidity:    ");
  Serial.print(humidity, 1);
  Serial.println(" %");
  Serial.println();
}
