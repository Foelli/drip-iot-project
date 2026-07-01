#include "moisturesensor.h"
#include "secrets.h"
#include <Arduino.h>

MoistureSensor::MoistureSensor()
    : moisturePin(A0), dryRaw(MOISTURE_DRY_RAW), wetRaw(MOISTURE_WET_RAW),
      lastRawValue(0), calibrationValid(false) {}

void MoistureSensor::init() {
  pinMode(moisturePin, INPUT);

  calibrationValid = dryRaw != wetRaw;

  Serial.println("Moisture sensor initialized");
  Serial.print("Dry raw: ");
  Serial.println(dryRaw);
  Serial.print("Wet raw: ");
  Serial.println(wetRaw);

  if (!calibrationValid) {
    Serial.println("Invalid moisture calibration: dry and wet raw values match.");
  }
}

void MoistureSensor::handleEvent(Event event) {}

int MoistureSensor::readMoisturePercent() {
  int raw = analogRead(moisturePin);
  lastRawValue = raw;

  if (!calibrationValid) {
    Serial.println("Moisture sensor");
    Serial.println("----------------");
    Serial.print("Raw:      ");
    Serial.println(raw);
    Serial.println("Moisture: invalid calibration");
    Serial.println();
    return -1;
  }

  int moisturePercent = map(raw, dryRaw, wetRaw, 0, 100);
  moisturePercent = constrain(moisturePercent, 0, 100);

  float voltage = raw * 3.3 / 1023.0;

  Serial.println("Moisture sensor");
  Serial.println("----------------");
  Serial.print("Raw:      ");
  Serial.println(raw);
  Serial.print("Voltage:  ");
  Serial.print(voltage, 3);
  Serial.println(" V");
  Serial.print("Dry raw:  ");
  Serial.println(dryRaw);
  Serial.print("Wet raw:  ");
  Serial.println(wetRaw);
  Serial.print("Moisture: ");
  Serial.print(moisturePercent);
  Serial.println(" %");
  Serial.println();

  return moisturePercent;
}

int MoistureSensor::getLastRawValue() const { return lastRawValue; }
