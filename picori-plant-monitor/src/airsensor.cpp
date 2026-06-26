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

float AirSensor::readTemperature() { return dht.readTemperature(); }

float AirSensor::readHumidity() { return dht.readHumidity(); }