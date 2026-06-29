#include "statusled.h"
#include <Arduino.h>

StatusLED::StatusLED() : state(OFF) {}

StatusLED::~StatusLED() {}

void StatusLED::init() {
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);
}

void StatusLED::handleEvent(Event event) {}

void StatusLED::toggle() {
  if (state == OFF) {
    digitalWrite(LED_BUILTIN, HIGH);
    state = ON;
  } else {
    digitalWrite(LED_BUILTIN, LOW);
    state = OFF;
  }
}
