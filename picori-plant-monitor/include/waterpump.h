#ifndef WATERPUMP_H
#define WATERPUMP_H

#include "smf.h"

class WaterPump : public Component {
public:
  WaterPump();

  void init() override;
  void handleEvent(Event event) override;
  void turnOn();
  void turnOff();
  void runFor(unsigned long durationMs);

private:
  int pumpPin;
};

#endif // WATERPUMP_H