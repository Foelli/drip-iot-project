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
  moistureSensor.readMoistureLevel();
  airSensor.readAirValues();
  delay(2000);
}
