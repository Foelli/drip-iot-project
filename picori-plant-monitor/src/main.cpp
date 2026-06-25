#include "application.h"
#include <Arduino.h>
#include <cmsis.h>

Firmware firmware;

void setup() { firmware.run(); }

void loop() {
  // Empty loop since the firmware.run() method contains an infinite loop
}