#include "application.h"
#include <Arduino.h>

void Firmware::init() {
  Serial.begin(9600);
  statusLED.init();
  Serial.println("Firmware started");
}

void Firmware::idle() {
  statusLED.toggle();
  delay(1000);
}
