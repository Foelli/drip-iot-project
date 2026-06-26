#ifndef APPLICATION_H
#define APPLICATION_H

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
};

#endif