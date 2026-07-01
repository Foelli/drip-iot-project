#include "waterpump.h"
#include "secrets.h"
#include <Arduino.h>

WaterPump::WaterPump() : pumpPin(PUMP_PIN) {}

void WaterPump::init() {
  pinMode(pumpPin, OUTPUT);
  turnOff();
  Serial.println("Water pump initialized");
  Serial.print("Pump pin: ");
  Serial.println(pumpPin);
}

void WaterPump::handleEvent(Event event) {}

void WaterPump::turnOn() {
  digitalWrite(pumpPin, HIGH);
  Serial.println("Water pump ON");
}

void WaterPump::turnOff() {
  digitalWrite(pumpPin, LOW);
  Serial.println("Water pump OFF");
}

void WaterPump::runFor(unsigned long durationMs) {
  turnOn();
  delay(durationMs);
  turnOff();
}
