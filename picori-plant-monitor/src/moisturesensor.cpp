#include "moisturesensor.h"
#include <Arduino.h>

MoistureSensor::MoistureSensor()
    : moisturePin(A0), dryValue(0), wetValue(1023) {}

void MoistureSensor::init() {
  Serial.println("Auto-calibrating moisture sensor");
  Serial.println("Move sensor between air/dry soil and wet soil/water.");
}

void MoistureSensor::handleEvent(Event event) {}

void MoistureSensor::readMoistureLevel() {
  int raw = analogRead(moisturePin);

  if (raw > dryValue) {
    dryValue = raw;
  }

  if (raw < wetValue) {
    wetValue = raw;
  }

  int moisturePercent = 0;

  if (dryValue != wetValue) {
    moisturePercent = map(raw, dryValue, wetValue, 0, 100);
    moisturePercent = constrain(moisturePercent, 0, 100);

    float voltage = raw * 3.3 / 1023.0;

    Serial.println("Moisture sensor");
    Serial.println("----------------");
    Serial.print("Raw:      ");
    Serial.println(raw);
    Serial.print("Voltage:  ");
    Serial.print(voltage, 3);
    Serial.println(" V");
    Serial.print("Dry:      ");
    Serial.println(dryValue);
    Serial.print("Wet:      ");
    Serial.println(wetValue);
    Serial.print("Moisture: ");
    Serial.print(moisturePercent);
    Serial.println(" %");
    Serial.println();
  }
}
