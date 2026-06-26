#ifndef APPLICATION_H
#define APPLICATION_H

#include "airsensor.h"
#include "moisturesensor.h"
#include "smf.h"
#include "statusled.h"

class Application {
public:
  void run();

  virtual void init() = 0;
  virtual void idle() = 0;

private:
  EventDispatcher eventDispatcher;
};

class Firmware : public Application {
public:
  void init();
  void idle();

private:
  StatusLED statusLED;
  MoistureSensor moistureSensor;
  AirSensor airSensor;
};

#endif
